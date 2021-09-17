package com.example.squraviewdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.squraviewdemo.model.Shape
import com.example.squraviewdemo.model.ShapePosition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * Create by lvwenrui on 2021/7/2 17:00
 * 六边形
 */
open class SquraView : View {

    companion object {
        //边长
        const val LINE_LENGTH = 40
        const val STROKE_WIDTH = 2
        const val COLOR_MODE_STROKE_WIDTH = 5

        /*内切圆半径*/
        val INNER_LINE_LENGTH: Int
            get() {
                return (LINE_LENGTH * cos(PI / 6)).roundToInt()
            }

    }


    //形状路径
    private var lineRect: Rect? = null
    private var path: Path? = null

    /*安装完*/
    private var COLOR_INSTALLED = Color.WHITE
    private var COLOR_INSTALLED_STROKEN = Color.WHITE

    /*安装中*/
    private var COLOR_INSTALLING = Color.WHITE
    private var COLOR_INSTALLING_STROKEN = Color.WHITE

    /*校准完*/
    private var COLOR_CHECKED = Color.WHITE
    private var COLOR_CHECKED_STROKEN = Color.WHITE

    /*校准中*/
    private var COLOR_CHECKING = Color.GREEN

    /*未校准*/
    private var COLOR_UNCHECKED = Color.WHITE
    private var COLOR_UNCHECKED_STROKEN = Color.WHITE

    /*默认*/
    private var COLOR_DEFAULT = Color.WHITE
    private var COLOR_DEFAULT_STROKRN = Color.WHITE


    /*获取世界坐标*/
    //中心点相对于父布局位置
    var pos: PointF? = null

    //画布旋转角度
    var canvasRotation = 0f

    /*自己的旋转偏移量*/
    var offsetRotation = 0f

    var nextDirectionTag: Int? = null//下一个的边的编号

    private var mColor: Int = COLOR_DEFAULT

    private var strokenColor: Int = COLOR_DEFAULT_STROKRN

    private var numText = ""

    private var textColor = Color.BLACK

    private var mBrightness = 0//亮度值

    public var mSelectted = false//是否选中

    private var mColorModeColor: Int? = null


    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }


    private fun init(context: Context?) {
        pos = PointF()
        path = Path()
        lineRect = Rect()

        textColor = context!!.resources.getColor(R.color.font_style_185_1_textColor)

        COLOR_INSTALLED = context.resources.getColor(R.color.ui_color_block_style_16_5_color)
        COLOR_INSTALLED_STROKEN =
            context.resources.getColor(R.color.ui_color_block_style_16_5_stroke)

        COLOR_INSTALLING = context.resources.getColor(R.color.ui_color_block_style_16_4_color)
        COLOR_INSTALLING_STROKEN =
            context.resources.getColor(R.color.ui_color_block_style_16_4_stroke)

        /*校准完固定白色*/
//        COLOR_CHECKED = context.resources.getColor(R.color.ui_color_block_style_16_1_color)
        COLOR_CHECKED_STROKEN = context.resources.getColor(R.color.ui_color_block_style_16_1_stroke)

        COLOR_UNCHECKED = context.resources.getColor(R.color.ui_color_block_style_16_2_color)
        COLOR_UNCHECKED_STROKEN =
            context.resources.getColor(R.color.ui_color_block_style_16_2_stroke)

        COLOR_DEFAULT = context.resources.getColor(R.color.ui_color_block_style_16_1_color)
        COLOR_DEFAULT_STROKRN = context.resources.getColor(R.color.ui_color_block_style_16_1_stroke)

        mColor = COLOR_DEFAULT
        strokenColor = COLOR_DEFAULT_STROKRN
    }

    /*设置世界坐标*/
    fun setPos(x: Float, y: Float) {
        pos!![x] = y
    }


    /*画编号*/
    fun setNumText(num: String) {
        if (numText != num) {
            this.numText = num
            invalidate()
        }
    }

    /*安装完状态*/
    fun change2InstalledState() {
        this.alpha = 1f
        /*填充颜色*/
        this.mColor = COLOR_INSTALLED
        this.strokenColor = COLOR_INSTALLED_STROKEN
        invalidate()
    }

    /*未安装完状态*/
    fun change2NextInstallState() {
        this.alpha = 1f
        this.mColor = COLOR_INSTALLING
        this.strokenColor = COLOR_INSTALLING_STROKEN
        invalidate()
    }

    /*校准中状态*/
    fun change2CheckingState() {
        this.mColor = COLOR_CHECKING
        /*TODO */
        this.strokenColor = COLOR_DEFAULT_STROKRN
        numText = ""
        invalidate()
    }

    /*校准完成状态*/
    fun change2CheckedState() {
        this.mColor = COLOR_CHECKED
        this.strokenColor = COLOR_CHECKED_STROKEN
        numText = ""
        invalidate()
    }

    fun change2UnCheckState(position : Int) {
        this.mColor = COLOR_UNCHECKED
        this.strokenColor = COLOR_UNCHECKED_STROKEN
        numText = "$position"
        invalidate()
    }

    /*设置颜色模式，亮度*/
    fun setColorMode4Brightness(brightness: Int) {
        isClickable = true
        strokenColor = COLOR_DEFAULT_STROKRN
        mBrightness = brightness
        invalidate()
    }

    /*设置颜色模式， 颜色*/
    fun setColorMode4Color(color: Int) {
        isClickable = true
        mColorModeColor = color
        strokenColor = COLOR_DEFAULT_STROKRN
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawShape(canvas, strokePaint)
    }

    /*画形状*/
    private fun drawShape(canvas: Canvas, strokePaint: Paint) {
        drawHexagon(canvas, strokePaint)
        drawNumText(canvas)
        drawColorMode(canvas)
    }

    /*画六边形*/
    private fun drawHexagon(canvas: Canvas, strokePaint: Paint) {
        val shapePoint = getShapePoint()
        path!!.moveTo(shapePoint[0][0], shapePoint[0][1])
        path!!.lineTo(shapePoint[1][0], shapePoint[1][1])
        path!!.lineTo(shapePoint[2][0], shapePoint[2][1])
        path!!.lineTo(shapePoint[3][0], shapePoint[3][1])
        path!!.lineTo(shapePoint[4][0], shapePoint[4][1])
        path!!.lineTo(shapePoint[5][0], shapePoint[5][1])
        path!!.close()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = mColorModeColor ?: mColor

        /*旋转*/
        val matrix = Matrix()
        matrix.setRotate(canvasRotation, width / 2f, height / 2f)

        /*画路径*/
        path!!.transform(matrix)
        canvas.drawPath(path!!, paint)
        canvas.drawPath(path!!, strokePaint)

        path!!.reset()
    }

    /*画编号*/
    private fun drawNumText(canvas: Canvas) {
        if (numText.isNotEmpty()) {
            val textPaint = Paint()
            textPaint.color = textColor
            textPaint.textSize = 20.8f
            textPaint.style = Paint.Style.FILL
            //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
            textPaint.textAlign = Paint.Align.CENTER

            drawTextOnCenter(canvas, textPaint, numText)
        }
    }

    /*画颜色模式*/
    private fun drawColorMode(canvas: Canvas) {
        if (!isClickable || mColorModeColor == null) return

        /*选中画框*/
        if (mSelectted) {
            drawSelectedStroke(canvas)
        }

        val textPaint = Paint()
        /*颜色反转*/
        val red = Color.red(mColorModeColor!!)
        val green = Color.green(mColorModeColor!!)
        val blue = Color.blue(mColorModeColor!!)
//        val textColor = UtilColor.toColor(255 - red, 255 - green, 255 - blue)
        if (red * 0.299 + green * 0.578 + blue * 0.114 >= 192) {
            //浅色
            textPaint.color = Color.BLACK
        }
        else {
            //深色
            textPaint.color = Color.WHITE
        }
        /*大小*/
        textPaint.textSize = 16f
        textPaint.style = Paint.Style.FILL
        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.textAlign = Paint.Align.CENTER
        drawTextOnCenter(canvas, textPaint, "${mBrightness}%")

    }

    /*画选中线*/
    private fun drawSelectedStroke(canvas: Canvas) {
        val shapePoint = getShapePoint(
            PointF((width / 2).toFloat(), (height / 2).toFloat()),
            LINE_LENGTH - STROKE_WIDTH / 2 - COLOR_MODE_STROKE_WIDTH / 2,
            0f
        )

        path!!.reset()

        path!!.moveTo(shapePoint[0][0], shapePoint[0][1])
        path!!.lineTo(shapePoint[1][0], shapePoint[1][1])
        path!!.lineTo(shapePoint[2][0], shapePoint[2][1])
        path!!.lineTo(shapePoint[3][0], shapePoint[3][1])
        path!!.lineTo(shapePoint[4][0], shapePoint[4][1])
        path!!.lineTo(shapePoint[5][0], shapePoint[5][1])
        path!!.close()


        /*旋转*/
        val matrix = Matrix()
        matrix.setRotate(canvasRotation, width / 2f, height / 2f)

        /*画路径*/
        path!!.transform(matrix)
        canvas.drawPath(path!!, colorModeStrokePaint)

        path!!.reset()
    }

    private fun drawTextOnCenter(canvas: Canvas, textPaint: Paint, text: String) {
        val fontMetrics = textPaint.fontMetrics
        val top = fontMetrics.top//为基线到字体上边框的距离,即上图中的top
        val bottom = fontMetrics.bottom//为基线到字体下边框的距离,即上图中的bottom

        val baseLineY = (height / 2).toFloat() - top / 2 - bottom / 2//基线中间点的y轴计算公式

        canvas.drawText(text, (width / 2).toFloat(), baseLineY, textPaint)
    }


    /*获取边框画笔*/
    private val strokePaint: Paint
        get() {
            val choosePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            choosePaint.color = strokenColor
            choosePaint.style = Paint.Style.STROKE
            choosePaint.strokeWidth = STROKE_WIDTH.toFloat()
            return choosePaint
        }

    /*获取边框画笔*/
    private val colorModeStrokePaint: Paint
        get() {
            val choosePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val near = isNearLowBlueColor(mColorModeColor!!)
            val colorRes =
                if (near) R.color.ui_color_block_style_16_3_stroke_near_blue else R.color.ui_color_block_style_16_3_stroke
            choosePaint.color = context.resources.getColor(colorRes)
            choosePaint.style = Paint.Style.STROKE
            choosePaint.strokeWidth = COLOR_MODE_STROKE_WIDTH.toFloat()
            return choosePaint
        }

    /**
     * 是否接近浅蓝色
     *
     * @param rgb
     * @return
     */
    fun isNearLowBlueColor(rgb: Int): Boolean {
        val hsv = FloatArray(3)
        Color.colorToHSV(rgb, hsv)
        val h = hsv[0].toInt()
        val s = (hsv[1] * 100).toInt()
        val v = (hsv[2] * 100).toInt()
        Log.e("TAG", "isNearLowBlueColor: h = $h ; s = $s ; v = $v")
        val nearH = 195 - 5 <= h && 195 + 5 >= h
        val nearS = 95 - 2 <= s && s <= 100
        val nearV = 82 - 2 <= v && v <= 100
        Log.e("TAG", "isNearLowBlueColor: nearH = $nearH ; nearS = $nearS ; nearV = $nearV")
        return nearH && nearS && nearV
    }

    /*形状的点*/
    private var mShapePoint: Array<FloatArray>? = null

    /*获取六边形的点*/
    private fun getShapePoint(): Array<FloatArray> {
        if (mShapePoint.isNullOrEmpty()) {
            mShapePoint = getShapePoint(
                PointF((width / 2).toFloat(), (height / 2).toFloat()),
                LINE_LENGTH,
                0f
            )
        }
        return mShapePoint!!
    }

    /*根据角度获取下一个shape的中点point*/
    fun getShapeRotation(pointF: PointF, length: Int, rotation: Float): PointF {
        val matrix = Matrix()
        matrix.setRotate(rotation + 30f, pointF.x, pointF.y)
        val fa = floatArrayOf(
            (length * cos(2 * PI * 4 / 6) + pointF.x).toFloat(),
            (length * sin(2 * PI * 4 / 6) + pointF.y).toFloat()
        )
        /*旋转*/
        matrix.mapPoints(fa)
        return PointF(fa[0], fa[1])

    }


    fun getShapePoint(pointF: PointF, length: Int, rotation: Float): Array<FloatArray> {
        val matrix = Matrix()
        matrix.setRotate(rotation, pointF.x, pointF.y)

        /*长度要减去边框宽度/2*/
        val realLength = length

        val shapePoint = Array(6) { FloatArray(2) }
        shapePoint[0] = floatArrayOf(
            (realLength * cos(2 * PI * 3 / 6) + pointF.x).toFloat(),
            (realLength * sin(2 * PI * 3 / 6) + pointF.y).toFloat()
        )
        shapePoint[1] = floatArrayOf(
            (realLength * cos(2 * PI * 4 / 6) + pointF.x).toFloat(),
            (realLength * sin(2 * PI * 4 / 6) + pointF.y).toFloat()
        )
        shapePoint[2] = floatArrayOf(
            (realLength * cos(2 * PI * 5 / 6) + pointF.x).toFloat(),
            (realLength * sin(2 * PI * 5 / 6) + pointF.y).toFloat()
        )
        shapePoint[3] = floatArrayOf(
            (realLength * cos(0.0) + pointF.x).toFloat(),
            (realLength * sin(0.0) + pointF.y).toFloat()
        )
        shapePoint[4] = floatArrayOf(
            (realLength * cos(2 * PI * 1 / 6) + pointF.x).toFloat(),
            (realLength * sin(2 * PI * 1 / 6) + pointF.y).toFloat()
        )
        shapePoint[5] = floatArrayOf(
            (realLength * cos(2 * PI * 2 / 6) + pointF.x).toFloat(),
            (realLength * sin(2 * PI * 2 / 6) + pointF.y).toFloat()
        )

        matrix.mapPoints(shapePoint[0])
        matrix.mapPoints(shapePoint[1])
        matrix.mapPoints(shapePoint[2])
        matrix.mapPoints(shapePoint[3])
        matrix.mapPoints(shapePoint[4])
        matrix.mapPoints(shapePoint[5])

        return shapePoint
    }

    private var tempArea = FloatArray(4)
    val shapeArea4Parent: FloatArray
        get() {
            val x = pos!!.x
            val y = pos!!.y
            tempArea[0] = x - LINE_LENGTH
            tempArea[1] = x + LINE_LENGTH
            tempArea[2] = y - LINE_LENGTH
            tempArea[3] = y + LINE_LENGTH
            return tempArea
        }

    val delArea4Parent: FloatArray
        get() {
            val x = pos!!.x
            val y = pos!!.y
            tempArea[0] = x - 20
            tempArea[1] = x + 20
            tempArea[2] = y - 20
            tempArea[3] = y + 20
            return tempArea
        }

    fun isPointInShapeArea(point: PointF): Boolean {
        return point.x >= shapeArea4Parent[0] && point.x <= shapeArea4Parent[1] &&
                point.y >= shapeArea4Parent[2] && point.y <= shapeArea4Parent[3]
    }


    /*获取传输协议时的旋转角度*/
    private fun getRotationValue(): Int {
        var angle = ((offsetRotation + canvasRotation).toInt() + 60) % 360
        while (angle < 0) {
            angle += 360
        }
        while (angle > 360) {
            angle -= 360
        }
        return angle
    }

    fun toShapePosition(): ShapePosition {
        return ShapePosition(Shape.HEXAGON, pos!!.x.toInt(), pos!!.y.toInt(), getRotationValue())
    }

    /**
     * 重置状态
     */
    fun reset() {
        path!!.reset()
        mColorModeColor = null
        canvasRotation = 0f
        /*自己的旋转偏移量*/
        offsetRotation = 0f
        nextDirectionTag = null//下一个的边的编号
    }


}