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
 * Fire-HD / low-RAM tuned PDF session.
 *
 * - One dedicated thread for PdfRenderer (API requirement)
 * - Render ARGB_8888 then store RGB_565 in cache (~½ RAM)
 * - Cap scale to screen fit (no oversized bitmaps)
 * - Tiny LRU: current + optional neighbor only
 */
class PdfDocumentSession(
    private val pfd: ParcelFileDescriptor,
    private val displayWidthPx: Int,
    private val displayHeightPx: Int,
) : Closeable {

    private val renderer = PdfRenderer(pfd)
    private val renderExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "pulse-pdf-render").apply { priority = Thread.NORM_PRIORITY - 1 }
    }
    private val renderDispatcher = renderExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + renderDispatcher)
    private val inFlight = ConcurrentHashMap<Int, Job>()

    val pageCount: Int get() = renderer.pageCount

    /** ~2 full-screen RGB_565 pages on 800×1280 ≈ 4MB; never exceed 1/16 of heap. */
    private val maxCacheBytes: Int = run {
        val fromHeap = (Runtime.getRuntime().maxMemory() / 16).toInt()
        min(fromHeap, 5 * 1024 * 1024)
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

    /** Prefetch at most the next page; drop everything else. */
    fun prefetchAround(center: Int) {
        val next = center + 1
        if (next in 0 until pageCount && getCached(next) == null) {
            requestPage(next) { _, _ -> }
        }
        synchronized(cache) {
            val keep = setOf(center, next).filter { it in 0 until pageCount }.toSet()
            for (k in cache.snapshot().keys.toList()) {
                if (k !in keep) cache.remove(k)
            }
        }
    }

    private fun renderPage(pageIndex: Int): Bitmap? {
        return try {
            val page = renderer.openPage(pageIndex)
            try {
                // Fit to screen only — zoom is matrix-based, no hi-res bitmap needed.
                val scale = min(
                    displayWidthPx.toFloat() / page.width,
                    displayHeightPx.toFloat() / page.height,
                ).coerceAtMost(1.25f)
                val w = max(1, (page.width * scale).roundToInt())
                val h = max(1, (page.height * scale).roundToInt())

                val argb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                argb.eraseColor(0xFFFFFFFF.toInt())
                page.render(argb, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Store half-size config for cache / ImageView.
                val rgb = argb.copy(Bitmap.Config.RGB_565, false)
                argb.recycle()
                rgb ?: return null
            } finally {
                page.close()
            }
        } catch (e: Exception) {
            android.util.Log.e("PulsePdf", "render failed page=$pageIndex", e)
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
