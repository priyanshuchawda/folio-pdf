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
 * Low-RAM PDF session for vertical continuous scrolling.
 * Pages are rendered to full display width; height follows aspect ratio.
 */
class PdfDocumentSession(
    private val pfd: ParcelFileDescriptor,
    private val displayWidthPx: Int,
    private val displayHeightPx: Int,
) : Closeable {

    private val renderer = PdfRenderer(pfd)
    private val renderExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "folio-pdf-render").apply { priority = Thread.NORM_PRIORITY - 1 }
    }
    private val renderDispatcher = renderExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + renderDispatcher)
    private val inFlight = ConcurrentHashMap<Int, Job>()

    val pageCount: Int get() = renderer.pageCount

    private val pageW = IntArray(pageCount)
    private val pageH = IntArray(pageCount)

    private val maxCacheBytes: Int = run {
        val fromHeap = (Runtime.getRuntime().maxMemory() / 16).toInt()
        // Vertical list may show ~1.5 pages; keep ~3 RGB_565 pages max
        min(fromHeap, 6 * 1024 * 1024)
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

    /** Load intrinsic page sizes once (for layout heights). */
    fun loadPageSizes(onDone: () -> Unit) {
        scope.launch {
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                pageW[i] = page.width
                pageH[i] = page.height
                page.close()
            }
            withContext(Dispatchers.Main.immediate) { onDone() }
        }
    }

    fun pageHeightForWidth(index: Int, widthPx: Int): Int {
        val w = pageW.getOrElse(index) { 0 }
        val h = pageH.getOrElse(index) { 0 }
        if (w <= 0 || h <= 0) {
            return max(1, (widthPx * 1.414f).roundToInt())
        }
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

    /** Keep current ± neighbors for vertical scroll. */
    fun prefetchAround(center: Int) {
        val keep = (center - 1..center + 2).filter { it in 0 until pageCount }.toSet()
        for (i in keep) {
            if (getCached(i) == null) requestPage(i) { _, _ -> }
        }
        synchronized(cache) {
            for (k in cache.snapshot().keys.toList()) {
                if (k !in keep) cache.remove(k)
            }
        }
    }

    private fun renderPage(pageIndex: Int): Bitmap? {
        return try {
            val page = renderer.openPage(pageIndex)
            try {
                // Fit to display WIDTH so pages stack naturally when scrolling down.
                val scale = (displayWidthPx.toFloat() / page.width).coerceAtMost(1.5f)
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
