package com.pulse.pdf.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

/**
 * Lightweight pinch-zoom / pan ImageView. No extra bitmaps — transforms only.
 */
class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatImageView(context, attrs) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val start = PointF()
    private val mid = PointF()
    private var mode = NONE
    private var minScale = 1f
    private var maxScale = 3f
    private var currentScale = 1f

    var onInteract: (() -> Unit)? = null
    var onSingleTap: (() -> Unit)? = null

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mode = ZOOM
                onInteract?.invoke()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                var factor = detector.scaleFactor
                val next = currentScale * factor
                if (next < minScale) factor = minScale / currentScale
                if (next > maxScale) factor = maxScale / currentScale
                matrix.postScale(factor, factor, detector.focusX, detector.focusY)
                currentScale *= factor
                imageMatrix = matrix
                return true
            }
        },
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onSingleTap?.invoke()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onInteract?.invoke()
                if (currentScale > minScale + 0.05f) {
                    resetZoom()
                } else {
                    matrix.postScale(2f, 2f, e.x, e.y)
                    currentScale = min(maxScale, currentScale * 2f)
                    imageMatrix = matrix
                }
                return true
            }
        },
    )

    init {
        scaleType = ScaleType.MATRIX
    }

    fun resetZoom() {
        currentScale = minScale
        fitCenter()
    }

    private fun fitCenter() {
        val d = drawable ?: return
        val vw = width.toFloat().takeIf { it > 0 } ?: return
        val vh = height.toFloat().takeIf { it > 0 } ?: return
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        if (dw <= 0 || dh <= 0) return
        val scale = min(vw / dw, vh / dh)
        minScale = scale
        currentScale = scale
        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate((vw - dw * scale) / 2f, (vh - dh * scale) / 2f)
        imageMatrix = matrix
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        post { fitCenter() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitCenter()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                onInteract?.invoke()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                savedMatrix.set(matrix)
                mid.set((event.getX(0) + event.getX(1)) / 2f, (event.getY(0) + event.getY(1)) / 2f)
                mode = ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG && !scaleDetector.isInProgress && currentScale > minScale) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(event.x - start.x, event.y - start.y)
                    imageMatrix = matrix
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }
        return true
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
