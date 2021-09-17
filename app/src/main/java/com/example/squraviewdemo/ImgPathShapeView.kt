package com.example.squraviewdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

/**
 * Create by lvwenrui on 2021/7/5 11:49
 * 路径图形
 */
open class ImgPathShapeView : androidx.appcompat.widget.AppCompatImageView {

    companion object {
        //边长
        const val SIZE_WIDTH = 19//边长
        const val SIZE_HEIGHT = 46//边长

    }

    //中心点相对于父布局位置，不是真的图形的中心点，是映射成六边形的中心点
    var pos: PointF? = null
    var mRotation = 0f//方向，绝对角度
    var numText = 0;//标号


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
        maxWidth = SIZE_WIDTH
        maxHeight = SIZE_HEIGHT
        scaleType = ScaleType.CENTER_INSIDE
        rotation = mRotation
        setImageResource(R.mipmap.new_light_6061_pics_buxian)
    }


    fun setData(angle : Float, numText : Int){
        this.mRotation = angle
        this.numText = numText
        rotation = mRotation

        if (numText == -1){
            setImageResource(R.mipmap.new_light_6061_pics_buxian_start)
        }
//        invalidate()
    }

    fun change2InstalledState(){
        this.alpha = 1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (numText != -1){
            drawNumText(canvas)
        }
    }

    private fun drawNumText(canvas: Canvas){
        val textPaint = Paint()
        textPaint.color = Color.parseColor("#ffffff")
        textPaint.textSize = 15f
        textPaint.style = Paint.Style.FILL
        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.textAlign = Paint.Align.CENTER

        var fontMetrics = textPaint.fontMetrics
        val top = fontMetrics.top //为基线到字体上边框的距离,即上图中的top
        val bottom = fontMetrics.bottom //为基线到字体下边框的距离,即上图中的bottom
        val textHeight = bottom - top //文本的高度，用于计算旋转中心
        val baseLineY = (height).toFloat() - bottom //基线中间点的y轴计算公式

        /*根据文本的中点进行旋转*/
        canvas.save()
        canvas.rotate(-mRotation, width/2f, height - textHeight/2f )
        canvas.drawText("$numText", width/2f, baseLineY, textPaint)
        canvas.restore()
    }


}