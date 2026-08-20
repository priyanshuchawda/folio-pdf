package com.pulse.pdf.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

/**
 * Keeps the screen awake only while reading.
 * Releases FLAG_KEEP_SCREEN_ON after [idleMs] to save battery on LCD tablets.
 */
class ScreenWakeGuard(
    private val activity: Activity,
    private val idleMs: Long = 45_000L,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var held = false
    private val release = Runnable { releaseWake() }

    fun onUserInteraction() {
        holdWake()
        handler.removeCallbacks(release)
        handler.postDelayed(release, idleMs)
    }

    fun holdWake() {
        if (!held) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            held = true
        }
    }

    fun releaseWake() {
        if (held) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            held = false
        }
    }

    fun dispose() {
        handler.removeCallbacks(release)
        releaseWake()
    }
}
