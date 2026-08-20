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
import androidx.core.view.isVisible
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import kotlin.math.max
import kotlin.math.min

/**
 * Drive-style fast scrubber on the right edge.
 * Drag to jump through long PDFs; shows current page while moving.
 */
class FolioScrollHandle(context: Context) : RelativeLayout(context), ScrollHandle {

    private var pdfView: PDFView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hide() }

    private val trackWidth = dp(18f)
    private val thumbMinHeight = dp(56f)
    private val margin = dp(4f)

    private val track = android.view.View(context).apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(9f).toFloat()
            setColor(0x33FFFFFF)
        }
    }

    private val thumb = TextView(context).apply {
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setPadding(dp(4f), dp(8f), dp(4f), dp(8f))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(10f).toFloat()
            setColor(0xE01B3A2F.toInt()) // Folio green
            setStroke(dp(1f), 0x66FFFFFF)
        }
        elevation = dp(3f).toFloat()
    }

    private var currentPage = 1
    private var dragging = false

    init {
        visibility = INVISIBLE
        clipChildren = false
        clipToPadding = false
        addView(track, LayoutParams(trackWidth, LayoutParams.MATCH_PARENT).apply {
            addRule(ALIGN_PARENT_RIGHT)
            rightMargin = margin
            topMargin = dp(48f)
            bottomMargin = dp(48f)
        })
        addView(thumb, LayoutParams(dp(40f), LayoutParams.WRAP_CONTENT).apply {
            addRule(ALIGN_PARENT_RIGHT)
            rightMargin = margin - dp(11f)
        })
    }

    override fun setupLayout(pdfView: PDFView) {
        this.pdfView = pdfView
        val lp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        // Overlay on top of PDFView (PDFView is a RelativeLayout)
        pdfView.addView(this, lp)
    }

    override fun destroyLayout() {
        pdfView?.removeView(this)
        pdfView = null
        handler.removeCallbacks(hideRunnable)
    }

    override fun setScroll(position: Float) {
        if (dragging) return
        val view = pdfView ?: return
        val usable = (track.height - thumb.height).coerceAtLeast(1)
        val y = track.top + position.coerceIn(0f, 1f) * usable
        thumb.y = y
        show()
        hideDelayed()
    }

    override fun setPageNum(pageNum: Int) {
        // library passes 1-based page in DefaultScrollHandle usage
        currentPage = pageNum
        val total = pdfView?.pageCount ?: 0
        thumb.text = if (total > 0) "$pageNum\n/ $total" else "$pageNum"
    }

    override fun shown(): Boolean = isVisible && visibility == VISIBLE

    override fun show() {
        visibility = VISIBLE
        alpha = 1f
    }

    override fun hide() {
        if (!dragging) visibility = INVISIBLE
    }

    override fun hideDelayed() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, 1800)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val view = pdfView ?: return super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!isOnHandle(event.x, event.y) && !isOnTrack(event.x, event.y)) {
                    return false
                }
                handler.removeCallbacks(hideRunnable)
                dragging = true
                show()
                moveTo(event.y, view)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return false
                moveTo(event.y, view)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!dragging) return false
                dragging = false
                hideDelayed()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun moveTo(rawY: Float, view: PDFView) {
        val usable = (track.height - thumb.height).coerceAtLeast(1)
        val y = min(
            (track.top + usable).toFloat(),
            max(track.top.toFloat(), rawY - thumb.height / 2f),
        )
        thumb.y = y
        val progress = ((y - track.top) / usable).coerceIn(0f, 1f)
        view.setPositionOffset(progress, false)
        val page = view.getPageAtPositionOffset(progress) + 1
        setPageNum(page)
    }

    private fun isOnHandle(x: Float, y: Float): Boolean {
        return x >= thumb.x - dp(8f) &&
            x <= thumb.x + thumb.width + dp(8f) &&
            y >= thumb.y - dp(8f) &&
            y <= thumb.y + thumb.height + dp(8f)
    }

    private fun isOnTrack(x: Float, y: Float): Boolean {
        return x >= track.x - dp(12f) &&
            x <= width &&
            y >= track.top &&
            y <= track.bottom
    }

    private fun dp(v: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()
}
