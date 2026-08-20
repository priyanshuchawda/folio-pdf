package com.pulse.pdf.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.ScrollHandle

/**
 * Drive-like fast scrubber: fat right-edge thumb with page number.
 * Drag up/down to jump through long PDFs.
 */
class FolioScrollHandle(context: Context) : RelativeLayout(context), ScrollHandle {

    private var pdfView: PDFView? = null
    private var relativeHandlerMiddle = 0f
    private var currentPos = 0f
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hide() }

    private val label = TextView(context).apply {
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setPadding(dp(6), dp(4), dp(6), dp(4))
    }

    init {
        visibility = INVISIBLE
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(dp(14).toFloat(), dp(14).toFloat(), 0f, 0f, 0f, 0f, dp(14).toFloat(), dp(14).toFloat())
            setColor(0xF01B3A2F.toInt())
            setStroke(dp(1), 0x66FFFFFF)
        }
        elevation = dp(4).toFloat()
        val lp = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { addRule(CENTER_IN_PARENT) }
        addView(label, lp)
        minimumWidth = dp(52)
        minimumHeight = dp(64)
    }

    override fun setupLayout(pdfView: PDFView) {
        this.pdfView = pdfView
        val w = dp(52)
        val h = dp(68)
        val params = LayoutParams(w, h).apply {
            addRule(ALIGN_PARENT_RIGHT)
            setMargins(0, 0, 0, 0)
        }
        pdfView.addView(this, params)
    }

    override fun destroyLayout() {
        pdfView?.removeView(this)
        pdfView = null
        hideHandler.removeCallbacks(hideRunnable)
    }

    override fun setScroll(position: Float) {
        if (!shown()) {
            show()
        } else {
            hideHandler.removeCallbacks(hideRunnable)
        }
        val view = pdfView ?: return
        if (view.isSwipeVertical) {
            y = view.height * position - relativeHandlerMiddle
        } else {
            x = view.width * position - relativeHandlerMiddle
        }
        // Keep on-screen
        if (view.isSwipeVertical) {
            y = y.coerceIn(0f, (view.height - height).toFloat().coerceAtLeast(0f))
        }
    }

    override fun setPageNum(pageNum: Int) {
        val total = pdfView?.pageCount ?: 0
        label.text = if (total > 0) "$pageNum / $total" else "$pageNum"
    }

    override fun shown(): Boolean = visibility == VISIBLE

    override fun show() {
        visibility = VISIBLE
    }

    override fun hide() {
        visibility = INVISIBLE
    }

    override fun hideDelayed() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 2500)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val view = pdfView ?: return super.onTouchEvent(event)
        if (!view.isLoaded || view.pageCount <= 0) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                hideHandler.removeCallbacks(hideRunnable)
                view.stopFling()
                currentPos = if (view.isSwipeVertical) event.rawY - y else event.rawX - x
                if (view.isSwipeVertical) {
                    relativeHandlerMiddle = event.y
                } else {
                    relativeHandlerMiddle = event.x
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val viewPos = if (view.isSwipeVertical) {
                    event.rawY - currentPos + relativeHandlerMiddle
                } else {
                    event.rawX - currentPos + relativeHandlerMiddle
                }
                val pdfPos = if (view.isSwipeVertical) {
                    (viewPos / view.height).coerceIn(0f, 1f)
                } else {
                    (viewPos / view.width).coerceIn(0f, 1f)
                }
                view.setPositionOffset(pdfPos, false)
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                hideDelayed()
                view.performPageSnap()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            resources.displayMetrics,
        ).toInt()
}

/** PDFView.stopFling is package-private in some builds — no-op safe helper. */
private fun PDFView.stopFling() {
    try {
        val m = javaClass.getDeclaredMethod("stopFling")
        m.isAccessible = true
        m.invoke(this)
    } catch (_: Exception) {
        // ignore
    }
}

private val PDFView.isLoaded: Boolean
    get() = try {
        pageCount > 0
    } catch (_: Exception) {
        false
    }
