package com.example.squraviewdemo

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import kotlin.math.sign

/**
 * Create by lvwenrui on 2021/7/1 19:52
 * 可以拖拽和拉伸的布局，内嵌一个View
 */
class ZoomLayout : FrameLayout, OnScaleGestureListener, GestureDetector.OnDoubleTapListener{
    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    private var mode = Mode.NONE
    var mScale = 1.0f
    private var lastScaleFactor = 0f

    // Where the finger first  touches the screen
    private var startX = 0f
    private var startY = 0f

    // How much to translate the canvas
    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

    private var scaleDetector: ScaleGestureDetector? = null
    private var doubleDetector : GestureDetector? = null
    var doubleClickListener: DoubleClick? = null
    var moveOrScaleListener: MoveOrScaleListener? = null

    constructor(context: Context?) : super(context!!) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init(context)
    }

    fun init(context: Context?) {
        setWillNotDraw(false)
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = true
        scaleDetector = ScaleGestureDetector(context, this)
        doubleDetector = GestureDetector(context, GestureDetector.SimpleOnGestureListener())
        doubleDetector?.setOnDoubleTapListener(this)
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "DOWN")
                if (mScale >= MIN_ZOOM) {
                    mode = Mode.DRAG
                    startX = motionEvent.x - prevDx
                    startY = motionEvent.y - prevDy
                }
            }
            MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG) {
                dx = motionEvent.x - startX
                dy = motionEvent.y - startY
            }
            MotionEvent.ACTION_POINTER_DOWN -> mode = Mode.ZOOM
            MotionEvent.ACTION_POINTER_UP ->                 /*缩放操作后不给连续滑动*/mode = Mode.NONE
            MotionEvent.ACTION_UP -> {
                Log.i(TAG, "UP")
                mode = Mode.NONE
                prevDx = dx
                prevDy = dy
            }
        }
        scaleDetector!!.onTouchEvent(motionEvent)
        doubleDetector?.onTouchEvent(motionEvent)
        if (mode == Mode.DRAG && mScale >= MIN_ZOOM || mode == Mode.ZOOM) {
            parent.requestDisallowInterceptTouchEvent(true)
            val maxDx = (child().width - (width - paddingLeft - paddingRight) / mScale) / 2 * mScale
            val maxDy =
                (child().height - (height - paddingTop - paddingBottom) / mScale) / 2 * mScale
            dx = Math.min(Math.max(dx, -maxDx), maxDx)
            dy = Math.min(Math.max(dy, -maxDy), maxDy)
            Log.i(
                TAG, "Width: " + child().width + ", scale " + mScale + ", dx " + dx
                        + ", max " + maxDx
            )
            Log.i(
                TAG, "Height: " + child().height + ", scale " + mScale + ", dy " + dy
                        + ", max " + maxDy
            )
            checkScaleRange()
            applyScaleAndTranslation(false)
        }
        awakenScrollBars()
        return true
    }

    override fun computeHorizontalScrollExtent(): Int {
        return (width / mScale).toInt()
    }

    override fun computeHorizontalScrollOffset(): Int {
        val offset = ((CanvasLayout.CANVAS_SIZE/2 - (dx + width/2)/mScale) ).toInt()
        Log.i(TAG, "computeVerticalScrollOffset dx = $dx  mScale = $mScale  offset = $offset")
        return offset
    }

    override fun computeHorizontalScrollRange(): Int {
        return CanvasLayout.CANVAS_SIZE
    }

    override fun computeVerticalScrollExtent(): Int {
        return (height / mScale).toInt()
    }

    override fun computeVerticalScrollOffset(): Int {
        val offset = ((CanvasLayout.CANVAS_SIZE/2 - (dy + height/2)/mScale) ).toInt()
        Log.i(TAG, "computeVerticalScrollOffset dy = $dy  mScale = $mScale  offset = $offset")
        return offset
    }

    override fun computeVerticalScrollRange(): Int {
        return CanvasLayout.CANVAS_SIZE
    }

    // ScaleGestureDetector
    override fun onScaleBegin(scaleDetector: ScaleGestureDetector): Boolean {
        Log.i(TAG, "onScaleBegin")
        return true
    }

    override fun onScale(scaleDetector: ScaleGestureDetector): Boolean {
        val scaleFactor = scaleDetector.scaleFactor
        Log.i(TAG, "onScale$scaleFactor")
        if (lastScaleFactor == 0f || sign(scaleFactor) == sign(lastScaleFactor)) {
            mScale *= scaleFactor
            val min = Math.max(MIN_ZOOM, width * 1f / child().width)
            mScale = Math.max(min, Math.min(mScale, MAX_ZOOM))
            lastScaleFactor = scaleFactor
        } else {
            lastScaleFactor = 0f
        }
        return true
    }

    override fun onScaleEnd(scaleDetector: ScaleGestureDetector) {
        Log.i(TAG, "onScaleEnd")
    }

    private fun checkScaleRange() {
        val min = MIN_ZOOM.coerceAtLeast(width * 1f / CanvasLayout.CANVAS_SIZE)
        mScale = min.coerceAtLeast(mScale.coerceAtMost(MAX_ZOOM))
    }

    private fun applyScaleAndTranslation(anima : Boolean) {
        if (anima){
            child().scaleX = mScale
            child().scaleY = mScale
            ViewCompat.animate(child())
                .translationX(dx)
                .translationY(dy)
                .start()
        }else{
            child().scaleX = mScale
            child().scaleY = mScale
            child().translationX = dx
            child().translationY = dy
        }
        moveOrScaleListener?.onMoveOrScale(mScale)
    }

    private fun child(): View {
        return getChildAt(0)
    }

    fun setScale(scale: Float) {
        mScale = scale
        checkScaleRange()
        applyScaleAndTranslation(false)
    }

    fun getCenterPoint() : Point{
        return Point((dx/mScale).toInt(), (dy/mScale).toInt())
    }

    /*x，y = 形状的偏移中点坐标*/
    fun setScaleAndTransitionXY(scale: Float, point: PointF) {
        if (scale <= 0f){
            setTransitionXY(point)
            return
        }
        mode = Mode.NONE
        mScale = scale
        checkScaleRange()
        lastScaleFactor = mScale
        startY = 0f
        startY = 0f
        dx = point.x * mScale
        dy = point.y * mScale
        prevDx = dx
        prevDy = dy
        applyScaleAndTranslation(false)
        Log.i(
            TAG,
            "scale=" + mScale + " dx=" + dx + " dy=" + dy + " pointX=" + point.x + " pointY=" + point.y
        )
    }

    fun setTransitionXY(point: PointF){
        mode = Mode.NONE
        checkScaleRange()
        lastScaleFactor = mScale
        startY = 0f
        startY = 0f
        dx = point.x * mScale
        dy = point.y * mScale
        prevDx = dx
        prevDy = dy
        applyScaleAndTranslation(true)
    }

    companion object {
        private const val TAG = "ZoomLayout"
        private const val MIN_ZOOM = 0.001f
        private const val MAX_ZOOM = 3.5f
    }

    interface DoubleClick{
        fun onDoubleClick(v : View)
    }
    interface MoveOrScaleListener{
        fun onMoveOrScale(scale : Float)
    }

    /*双击事件*/
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.i(TAG, "onSingleTapConfirmed")
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.i(TAG, "onDoubleTap")
        doubleClickListener?.onDoubleClick(this)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.i(TAG, "onDoubleTapEvent")
        return true
    }


}