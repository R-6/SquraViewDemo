package com.example.squraviewdemo

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet

/**
 * Create by lvwenrui on 2021/7/5 11:49
 * 电源图形
 */
open class ImgPowerShapeView : androidx.appcompat.widget.AppCompatImageView {

    companion object {
        //边长
        const val SIZE_POWER = 38//边长
        const val PADDING = 9//内边距
    }

    //中心点相对于父布局位置，不是真的图形的中心点，是映射成六边形的中心点
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
        maxWidth = SIZE_POWER
        maxHeight = SIZE_POWER
        setPadding(PADDING, PADDING, PADDING, PADDING)
        scaleType = ScaleType.FIT_CENTER
        setImageResource(R.mipmap.new_light_6061_icon_power)
    }


}