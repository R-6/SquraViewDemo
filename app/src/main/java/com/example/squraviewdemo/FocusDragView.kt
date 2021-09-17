package com.example.squraviewdemo

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent


/**
 * Create by lvwenrui on 2021/7/13 16:45
 */
class FocusDragView : androidx.appcompat.widget.AppCompatImageView {

    companion object {
        //边长
        const val SIZE_FOCUS = 56//边长
    }

    private var moveX = 0f
    private var moveY = 0f

    var pos: PointF? = null

    var focusDragCallback : ((PointF) -> Unit)? = null//拖拽回调

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init(){

        maxWidth = SIZE_FOCUS
        maxHeight = SIZE_FOCUS
        scaleType = ScaleType.FIT_CENTER
        setImageResource(R.mipmap.new_btn_fangxiang_zhongxin_nei)
    }

    /*设置聚拢&扩散 true/false*/
    fun setIsIn(isIn : Boolean){
        setImageResource(if (isIn) R.mipmap.new_btn_fangxiang_zhongxin_nei else R.mipmap.new_btn_fangxiang_zhongxin)
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        /*向父布局申请不拦截触摸事件*/
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                moveX = event.x
                moveY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                translationX = x + (event.x - moveX) - left
                translationY = y + (event.y - moveY) - top

                Log.i("FocusDragView", "translationX=${translationX}  x=${x}  event.x=${event.x}  moveX=${moveX}")

            }
            MotionEvent.ACTION_UP -> {
                pos?.set(
                    left + SIZE_FOCUS / 2f + translationX,
                    top + SIZE_FOCUS / 2f + translationY
                )
                Log.i("FocusDragView", "left=${left}  top=${top}  translationX=${translationX}  translationY=${translationY}")
                focusDragCallback?.invoke(pos!!)
            }
            MotionEvent.ACTION_CANCEL -> {
                pos?.set(
                    left + SIZE_FOCUS / 2f + translationX,
                    top + SIZE_FOCUS / 2f + translationY
                )
                focusDragCallback?.invoke(pos!!)
            }
        }
        return true
    }



}