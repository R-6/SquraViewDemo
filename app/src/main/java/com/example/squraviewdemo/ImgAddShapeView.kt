package com.example.squraviewdemo

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet

/**
 * Create by lvwenrui on 2021/7/5 11:49
 * 添加shape按钮
 */
open class ImgAddShapeView : androidx.appcompat.widget.AppCompatImageView {

    companion object {
        //边长
        const val SIZE_ADD = 38
    }

    //中心点相对于父布局位置
    var pos: PointF? = null
    var directionTag = 0//方向，绝对角度


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
        maxWidth = SIZE_ADD
        maxHeight = SIZE_ADD
        scaleType = ScaleType.FIT_CENTER
        setImageResource(R.mipmap.new_btn_6061_add_bian)
    }


}