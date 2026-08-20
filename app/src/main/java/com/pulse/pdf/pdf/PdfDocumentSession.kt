package com.pulse.pdf.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Instant-open PDF session (Google Drive style).
 *
 * - Never walks all pages at open (critical for 1000+ page docs)
 * - Seeds height from page 0 only; assumes uniform pages (normal for textbooks)
 * - Renders only visible neighbors; cancels stale work while flinging
 * - RGB_565 cache, width-fit bitmaps for vertical scroll
 */
class PdfDocumentSession(
    private val pfd: ParcelFileDescriptor,
    private val displayWidthPx: Int,
    @Suppress("UNUSED_PARAMETER") private val displayHeightPx: Int,
) : Closeable {

    private val renderer = PdfRenderer(pfd)
    private val renderExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "folio-pdf-render").apply { priority = Thread.NORM_PRIORITY - 1 }
    }
    private val renderDispatcher = renderExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + renderDispatcher)
    private val inFlight = ConcurrentHashMap<Int, Job>()

    val pageCount: Int = renderer.pageCount

    /** Default page size from page 0 (or A4-ish fallback). */
    @Volatile private var defaultW: Int = 612
    @Volatile private var defaultH: Int = 792
    @Volatile private var seeded: Boolean = false

    private val maxCacheBytes: Int = run {
        val fromHeap = (Runtime.getRuntime().maxMemory() / 12).toInt()
        min(fromHeap, 8 * 1024 * 1024)
    }

    private val cache = object : LruCache<Int, Bitmap>(maxCacheBytes) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
        override fun entryRemoved(
            evicted: Boolean,
            key: Int,
            oldValue: Bitmap,
            newValue: Bitmap?,
        ) {
            if (evicted && !oldValue.isRecycled && newValue !== oldValue) {
                oldValue.recycle()
            }
        }
    }

    /**
     * Seed layout size from page 0 only — O(1) open, then UI can show immediately.
     * Call once; safe to call again (no-op if already seeded).
     */
    fun seedDefaultSize(onDone: () -> Unit) {
        if (seeded || pageCount <= 0) {
            onDone()
            return
        }
        scope.launch {
            try {
                val page = renderer.openPage(0)
                defaultW = page.width.coerceAtLeast(1)
                defaultH = page.height.coerceAtLeast(1)
                page.close()
                seeded = true
            } catch (e: Exception) {
                android.util.Log.w("Folio", "seed size failed, using A4 defaults", e)
                seeded = true
            }
            withContext(Dispatchers.Main.immediate) { onDone() }
        }
    }

    fun pageHeightForWidth(widthPx: Int): Int {
        val w = defaultW.coerceAtLeast(1)
        val h = defaultH.coerceAtLeast(1)
        return max(1, (widthPx.toFloat() * h / w).roundToInt())
    }

    fun getCached(pageIndex: Int): Bitmap? = synchronized(cache) {
        cache.get(pageIndex)?.takeIf { !it.isRecycled }
    }

    fun requestPage(
        pageIndex: Int,
        onReady: (Int, Bitmap) -> Unit,
    ) {
        if (pageIndex !in 0 until pageCount) return
        getCached(pageIndex)?.let {
            onReady(pageIndex, it)
            return
        }
        if (inFlight.containsKey(pageIndex)) return

        val job = scope.launch {
            val bmp = renderPage(pageIndex) ?: return@launch
            synchronized(cache) { cache.put(pageIndex, bmp) }
            withContext(Dispatchers.Main.immediate) {
                onReady(pageIndex, bmp)
            }
        }
        inFlight[pageIndex] = job
        job.invokeOnCompletion { inFlight.remove(pageIndex) }
    }

    /**
     * Prefetch around [center]; cancel only far-away work so flinging
     * a 1000-page doc does not queue hundreds of renders — but never
     * cancel the page the user is looking at (or its neighbors).
     */
    fun prefetchAround(center: Int) {
        val keep = (center - 1..center + 2).filter { it in 0 until pageCount }.toSet()
        val cancelBeyond = 6

        for ((idx, job) in inFlight.toMap()) {
            if (kotlin.math.abs(idx - center) > cancelBeyond) job.cancel()
        }

        // Visible first, then neighbors
        val order = listOf(center, center + 1, center - 1, center + 2)
            .filter { it in 0 until pageCount }
        for (i in order) {
            if (getCached(i) == null) requestPage(i) { _, _ -> }
        }
        synchronized(cache) {
            for (k in cache.snapshot().keys.toList()) {
                if (k !in keep && kotlin.math.abs(k - center) > cancelBeyond) {
                    cache.remove(k)
                }
            }
        }
    }

    private fun renderPage(pageIndex: Int): Bitmap? {
        return try {
            val page = renderer.openPage(pageIndex)
            try {
                // Update defaults if this page differs (rare); layout still uses first seed.
                if (!seeded) {
                    defaultW = page.width.coerceAtLeast(1)
                    defaultH = page.height.coerceAtLeast(1)
                    seeded = true
                }
                val scale = (displayWidthPx.toFloat() / page.width).coerceIn(0.25f, 1.5f)
                val w = max(1, (page.width * scale).roundToInt())
                val h = max(1, (page.height * scale).roundToInt())

                val argb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                argb.eraseColor(0xFFFFFFFF.toInt())
                page.render(argb, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                val rgb = argb.copy(Bitmap.Config.RGB_565, false)
                argb.recycle()
                rgb
            } finally {
                page.close()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("Folio", "render failed page=$pageIndex", e)
            null
        }
    }

    fun trimMemory() {
        inFlight.values.forEach { it.cancel() }
        inFlight.clear()
        synchronized(cache) { cache.evictAll() }
    }

    override fun close() {
        scope.cancel()
        inFlight.clear()
        synchronized(cache) { cache.evictAll() }
        try {
            renderer.close()
        } catch (_: Exception) {
        }
        try {
            pfd.close()
        } catch (_: Exception) {
        }
        renderDispatcher.close()
        renderExecutor.shutdownNow()
    }
}
