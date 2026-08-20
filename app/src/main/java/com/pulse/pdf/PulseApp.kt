package com.pulse.pdf

import android.app.Application
import android.content.ComponentCallbacks2

class PulseApp : Application() {
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            PdfSession.trimCaches()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        PdfSession.trimCaches()
    }
}
