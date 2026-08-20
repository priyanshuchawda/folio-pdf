package com.pulse.pdf

import android.app.Application
import android.content.ComponentCallbacks2
import com.github.barteksc.pdfviewer.util.Constants

/**
 * Folio process defaults — tuned for ~1–2 GB tablets (e.g. Fire HD 8).
 * Keeps Pdfium page-cache small so large Telegram/Drive PDFs stay smooth
 * without thrashing memory or killing battery.
 */
class PulseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        applyLowRamPdfProfile()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            // Shrink preload so the OS can reclaim page bitmaps sooner.
            Constants.PRELOAD_OFFSET = 4
            Constants.Cache.CACHE_SIZE = 32
        }
    }

    companion object {
        fun applyLowRamPdfProfile() {
            // Default CACHE_SIZE is 120 parts — too heavy on 1.4 GB devices.
            Constants.Cache.CACHE_SIZE = 48
            Constants.Cache.THUMBNAILS_CACHE_SIZE = 4
            Constants.PRELOAD_OFFSET = 8
            Constants.THUMBNAIL_RATIO = 0.22f
            Constants.DEBUG_MODE = false
        }
    }
}
