package com.example.squraviewdemo

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.example.squraviewdemo.model.ShapePosition
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Create by lvwenrui on 2021/7/3 10:58
 * 画布的布局，
 */
class CanvasLayout : FrameLayout {
    @kotlin.jvm.JvmField
    val Tag = this.javaClass.name

    companion object {
        const val MAX_COUNT = 23
        const val CANVAS_SIZE = 3200//画布布局
        const val CANVAS_PADDING = 40//边界检测的padding
        const val SCALE_DEFAULT = 1.5f//初始化时拉伸的比例

        /*创建默认Shape*/
        @JvmStatic
        fun generaDefaultShape(type: Int): ShapePosition {
            return ShapePosition(type, CANVAS_SIZE / 2, CANVAS_SIZE / 2, 60)//默认60是正的
        }

    }

    /*形状列表*/
    var shapeViews: MutableList<SquraView> = ArrayList()

    /*添加按钮View的集合*/
    var addViews: MutableList<View> = ArrayList()

    /*路径的View的集合*/
    var pathViews: MutableList<ImgPathShapeView> = ArrayList()

    /*删除按钮*/
    var delView: View? = null

    /*电源View*/
    var powerView: ImgPowerShapeView? = null

    /*聚焦View*/
    var focusDragView: FocusDragView? = null
    var isIn = false//聚焦，扩撒

    //整体旋转角度
    private var canvasRotation = 0f

    /*整个图形中心点*/
    private var mShapeCenterPoint: PointF? = null

    /*图形初始的相对于画布中心的偏移*/
    public var mDefaultOffsetPoint : PointF? = null

    /*聚焦view的scale*/
    private var mFocusDragViewScale : Float = 0f

    /*UI类型*/
    var mType = Type.defaultType

    /*UI类型*/
    enum class Type {
        defaultType,//默认，白色块
        edit,//拼接
        install,//安装
        check,//校验
        colorMode,///颜色模式
        focus,///diy&场景聚焦模式
    }


    /*缩放和拖动回调*/
    var callBack: OnScaleAndTransitionChange? = null
    var colorModelCallback: OnSelectShapeChange? = null
    var focusDragCallback: OnFocusDragChange? = null
    var addDeleteCallback: OnAddDeleteChange? = null

    private val lastedPointF: PointF?
        get() {
            return shapeViews.lastOrNull()?.pos
        }

    private val lastedShape: SquraView?
        get() {
            return shapeViews.lastOrNull()
        }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    fun init() {
        val lp: FrameLayout.LayoutParams? = layoutParams as? LayoutParams
        lp?.apply {
            this.width = CANVAS_SIZE
            this.height = CANVAS_SIZE
            this.gravity = Gravity.CENTER
            layoutParams = this
        }
        clipChildren = false
    }

    /*添加，方块灯 direction 方向，Tag 0-5*/
    private fun addShape(directionTag: Int, updateOtherUi: Boolean): FloatArray {
        Log.i("addShape", "directionTag = $directionTag")

        val squraView = SquraView(context)
        squraView.canvasRotation = canvasRotation

        /*上一个的Offset*/
        var lastRotation = 0f

        lastedShape?.apply {
            /*相对偏移角度*/
            this.nextDirectionTag = directionTag
            lastRotation = this.offsetRotation
        }

        val layoutParams = LayoutParams(SquraView.LINE_LENGTH * 2, SquraView.LINE_LENGTH * 2)
        val centerPointX: Float
        val centerPointY: Float
        if (lastedPointF == null) {
            /*添加第一个*/
            centerPointX = (CANVAS_SIZE / 2).toFloat()
            centerPointY = (CANVAS_SIZE / 2).toFloat()
        }
        else {
            /*计算角度*/
            /*实际偏移角度 = 旋转过的角度 + 添加的方向角度*/
            val realRotation = lastRotation + canvasRotation + directionTag * 60 - 60

            val point =
                squraView.getShapeRotation(lastedPointF!!,
                    SquraView.INNER_LINE_LENGTH * 2,
                    realRotation)

            centerPointX = point.x
            centerPointY = point.y
        }

        squraView.setPos(centerPointX, centerPointY)
        squraView.offsetRotation = lastRotation + directionTag * 60 - 60

        /*安装模式设置方块颜色透明*/
        if (mType == Type.install) {
            squraView.alpha = 0.4f
        }

        /*颜色模式*/
        if (mType == Type.colorMode) {
            val color = resources.getColor(R.color.white)
            squraView.setColorMode4Color(color)
//            squraView.setColorMode4Brightness(100)
            squraView.setOnClickListener {
                squraView.mSelectted = !squraView.mSelectted
                squraView.invalidate()
                /*通知选中方块*/
                val list = arrayListOf<Boolean>()
                shapeViews.forEach {
                    list.add(it.mSelectted)
                }
                colorModelCallback?.selectedShape(list)
            }
        }

        layoutParams.leftMargin = centerPointX.toInt() - SquraView.LINE_LENGTH
        layoutParams.topMargin = centerPointY.toInt() - SquraView.LINE_LENGTH
        addView(squraView, layoutParams)

        shapeViews.add(squraView)

        if (updateOtherUi) {
            updateUIState()
        }

        /*返回view的四条边坐标*/
        return floatArrayOf(
            centerPointX - SquraView.LINE_LENGTH,
            centerPointX + SquraView.LINE_LENGTH,
            centerPointY - SquraView.LINE_LENGTH,
            centerPointY + SquraView.LINE_LENGTH,
        )
    }

    /*根据协议中定义的角度来添加图形*/
    private fun addShapeWithAngle(angle: Int, updateOtherUi: Boolean): FloatArray {
        /*上一个的Offset*/
        val lastOffsetRotation = lastedShape?.offsetRotation ?: 0f

        var directionTag = ((angle - canvasRotation - lastOffsetRotation) / 60).toInt()

        while (directionTag < 0) directionTag += 6

        return addShape(directionTag, updateOtherUi)
    }


    /*删除，最后一个图形*/
    private fun delLastedShape() {
        if (shapeViews.size <= 1) return

        removeView(lastedShape)
        shapeViews.removeLastOrNull()

        updateUIState()
        addDeleteCallback?.shapeSizeChange(shapeViews.size)
    }

    /*旋转，遍历所有view，重绘 rotation一次旋转的角度*/
    fun setShapeRotation(oneceRotation: Float) {
        canvasRotation += oneceRotation
        canvasRotation %= 360

        var lastShape: SquraView? = null
        shapeViews.apply {
            for (shape in this) {
                shape.canvasRotation = canvasRotation

                lastShape?.apply {
                    /*转换坐标*/
                    val needRota =
                        (this.offsetRotation + this@CanvasLayout.canvasRotation + (this.nextDirectionTag
                            ?: 1) * 60 - 60)
                    val point =
                        shape.getShapeRotation(this.pos!!,
                            SquraView.INNER_LINE_LENGTH * 2,
                            needRota)

                    shape.setPos(point.x, point.y)
                }
                updateShapePosition(shape)
                lastShape = shape
            }

            updateUIState()
        }
    }

    private fun updateUIState() {
        if (mType == Type.edit || mType == Type.install || mType == Type.check) {
            /*更新电源键*/
            updatePowerItem()
        }
        if (mType == Type.edit) {
            /*更新添加按钮*/
            updateAddImgItem()
            /*更新删除按钮*/
            updateDelState()
        }

        if (mType == Type.install) {
            /*更新图形的拼接路径*/
            updatePathItem()
        }

        if (mType == Type.focus) {
            /*更新拖拽聚焦item*/
            updateFocusDragItem()
        }
    }

    /*更新某个图形的具体位置*/
    private fun updateShapePosition(shape: SquraView) {
        val layoutParams: LayoutParams? = shape.layoutParams as? LayoutParams
        shape.pos?.apply {
            layoutParams?.leftMargin = (this.x.toInt() - SquraView.LINE_LENGTH)
            layoutParams?.topMargin = (this.y.toInt() - SquraView.LINE_LENGTH)
            shape.layoutParams = layoutParams
        }
        shape.invalidate()
    }

    /*更新删除按钮*/
    private fun updateDelState() {
        /*最后一个图形可以删除*/
        var num = 1
        shapeViews.forEach {
            it.setNumText("${num++}")
        }
        /*清除最后一个删除*/
        delView?.let { removeView(it) }
        delView = null

        if (shapeViews.size > 1) {
            lastedShape?.setNumText("")
            /*删除按钮*/
            val imgDelView = ImgDelShapeView(context)
            imgDelView.pos = lastedPointF
            val layoutParams = LayoutParams(ImgDelShapeView.SIZE_DEL, ImgDelShapeView.SIZE_DEL)
            layoutParams.leftMargin = lastedPointF!!.x.toInt() - ImgDelShapeView.SIZE_DEL / 2
            layoutParams.topMargin = lastedPointF!!.y.toInt() - ImgDelShapeView.SIZE_DEL / 2
            addView(imgDelView, layoutParams)
            /*点击监听*/
            imgDelView.setOnClickListener { delLastedShape() }
            delView = imgDelView
        }

    }

    /*更新添加按钮数量及位置*/
    private fun updateAddImgItem() {
        /*清空所有添加按钮*/
        addViews.forEach {
            removeView(it)
        }
        addViews.clear()
        //不可用区域
        if (lastedShape == null || shapeViews.size >= MAX_COUNT) return
        /*根据最后一个添加的去做操作*/
        val area = getSelectedArea()
        val points =
            lastedShape!!.getShapePoint(lastedShape!!.pos ?: PointF(),
                SquraView.INNER_LINE_LENGTH + ImgAddShapeView.SIZE_ADD / 2,
                lastedShape!!.offsetRotation + lastedShape!!.canvasRotation + 30)
        for (i in 0..5) {
            val center = PointF(points[i][0], points[i][1])
            if (checkAreaValid(center, area)) {
                /*有效范围内，可以添加*/
                val imgView = ImgAddShapeView(context)
                imgView.pos = center
                imgView.directionTag = i
                val layoutParams = LayoutParams(ImgAddShapeView.SIZE_ADD, ImgAddShapeView.SIZE_ADD)
                layoutParams.leftMargin = center.x.toInt() - ImgAddShapeView.SIZE_ADD / 2
                layoutParams.topMargin = center.y.toInt() - ImgAddShapeView.SIZE_ADD / 2
                addView(imgView, layoutParams)
                addViews.add(imgView)
                /*点击监听*/
                imgView.setOnClickListener { onAddItemClick(imgView.directionTag) }
            }
        }
    }


    /*更新电源位置*/
    private fun updatePowerItem() {
        /*清除最后一个删除*/
        powerView?.let { removeView(it) }
        powerView = null
        shapeViews.getOrNull(0)?.apply {
            /*第一个*/
            val needRota = (this@CanvasLayout.canvasRotation + 180)
            val point =
                this.getShapeRotation(this.pos!!,
                    SquraView.INNER_LINE_LENGTH + ImgPowerShapeView.SIZE_POWER / 2,
                    needRota)

            /*电源图标占的格子的中心点*/
            val shapePoint =
                this.getShapeRotation(this.pos!!, SquraView.INNER_LINE_LENGTH * 2, needRota)
            powerView = ImgPowerShapeView(context)
            powerView!!.pos = shapePoint
            /*处于绘制最高级*/
            powerView!!.z = 2f
            val layoutParams =
                LayoutParams(ImgPowerShapeView.SIZE_POWER, ImgPowerShapeView.SIZE_POWER)
            layoutParams.leftMargin = point.x.toInt() - ImgPowerShapeView.SIZE_POWER / 2
            layoutParams.topMargin = point.y.toInt() - ImgPowerShapeView.SIZE_POWER / 2
            addView(powerView, layoutParams)

        }
    }

    /*更新路径数量及位置*/
    private fun updatePathItem() {
        /*清空所有添加按钮*/
        pathViews.forEach {
            removeView(it)
        }
        pathViews.clear()

        if (lastedShape == null || shapeViews.size > MAX_COUNT) return

        var lastShape: SquraView? = null
        for (curShape in shapeViews) {
            if (lastShape == null) {
                /*画路径图标*/
                val pathView = ImgPathShapeView(context)
                val centerX = (curShape.pos!!.x + powerView?.pos!!.x) / 2
                val centerY = (curShape.pos!!.y + powerView?.pos!!.y) / 2
                pathView.pos = PointF(centerX, centerY)

                pathView.setData(canvasRotation, -1)

                val layoutParams =
                    LayoutParams(ImgPathShapeView.SIZE_WIDTH, ImgPathShapeView.SIZE_HEIGHT)
                layoutParams.leftMargin = centerX.toInt() - ImgPathShapeView.SIZE_WIDTH / 2
                layoutParams.topMargin = centerY.toInt() - ImgPathShapeView.SIZE_HEIGHT / 2
                addView(pathView, layoutParams)
                pathViews.add(pathView)

            }
            else {
                val pathView = ImgPathShapeView(context)
                val centerX = (curShape.pos!!.x + lastShape.pos!!.x) / 2
                val centerY = (curShape.pos!!.y + lastShape.pos!!.y) / 2
                pathView.pos = PointF(centerX, centerY)
                val realRotation =
                    lastShape.canvasRotation + lastShape.offsetRotation + (lastShape.nextDirectionTag!! * 60 - 60)
                /*画路径view*/
                /*实际灯的编号位置*/
                var pathNumTag = (lastShape.nextDirectionTag ?: -2) + 2
                while (pathNumTag >= 6) pathNumTag -= 6
                /*取反*/
                pathNumTag = 6 - pathNumTag
                Log.i(Tag, "directionTag = $pathNumTag")
                pathView.setData(realRotation, pathNumTag)

                if (mType == Type.install && pathViews.size >= 2) {
                    pathView.alpha = 0.4f
                }

                val layoutParams =
                    LayoutParams(ImgPathShapeView.SIZE_WIDTH, ImgPathShapeView.SIZE_HEIGHT)
                layoutParams.leftMargin = centerX.toInt() - ImgPathShapeView.SIZE_WIDTH / 2
                layoutParams.topMargin = centerY.toInt() - ImgPathShapeView.SIZE_HEIGHT / 2
                addView(pathView, layoutParams)
                pathViews.add(pathView)
            }

            lastShape = curShape

        }
    }

    /*返回选中的点*/
    private fun updateFocusDragItem(): Point {
        /*图标占的整个图形的中心点*/
        updateFocusDragItem(mShapeCenterPoint!!)
        return Point(mShapeCenterPoint!!.x.toInt(), mShapeCenterPoint!!.y.toInt())
    }

    /*更新聚焦item位置*/
    private fun updateFocusDragItem(selectPoint: PointF) {
        /*清除最后一个删除*/
        focusDragView?.let { removeView(it) }
        focusDragView = null
        /*图标占的整个图形的中心点*/
        focusDragView = FocusDragView(context)
        focusDragView!!.pos = selectPoint
        /*处于绘制最高级*/
        focusDragView!!.z = 2f
        /*聚焦&扩散*/
        focusDragView!!.setIsIn(isIn)
        /*拖拽回调*/
        focusDragView!!.focusDragCallback = { pointF: PointF ->
            focusDragCallback?.focusDragPointChange(pointF)
        }
        val layoutParams = LayoutParams(FocusDragView.SIZE_FOCUS, FocusDragView.SIZE_FOCUS)
        layoutParams.leftMargin = selectPoint.x.toInt() - FocusDragView.SIZE_FOCUS / 2
        layoutParams.topMargin = selectPoint.y.toInt() - FocusDragView.SIZE_FOCUS / 2
        addView(focusDragView, layoutParams)
        updateDragViewScale(mFocusDragViewScale)

//        if (needNotify){
//            /*第一次返回选中位置*/
//            focusDragCallback?.focusDragPointChange(selectPoint)
//        }
    }

    /*获取到已占用的区域*/
    private fun getSelectedArea(): Array<FloatArray> {
        val arrayArea =
            Array(shapeViews.size + if (powerView?.pos != null) 1 else 0) { FloatArray(4) }
        for (i in shapeViews.size - 1 downTo 0) {
            arrayArea[i] = shapeViews[i].shapeArea4Parent
        }
        /*加上电源位置*/
        powerView?.pos?.apply {
            arrayArea[shapeViews.size] = floatArrayOf(
                x - SquraView.LINE_LENGTH,
                x + SquraView.LINE_LENGTH,
                y - SquraView.LINE_LENGTH,
                y + SquraView.LINE_LENGTH
            )
        }
        return arrayArea
    }

    /*检查点是否在有效区域内*/
    private fun checkAreaValid(point: PointF, area: Array<FloatArray>): Boolean {
        area.forEach {
            if (point.x >= it[0] && point.x <= it[1] && point.y >= it[2] && point.y <= it[3]) {
                return false
            }
        }
        /*是否在在画布范围内*/
        if (point.x - CANVAS_PADDING <= 0
            || point.x + CANVAS_PADDING >= CANVAS_SIZE
            || point.y - CANVAS_PADDING <= 0
            || point.y + CANVAS_PADDING >= CANVAS_SIZE
        ) {
            return false
        }
        return true
    }

    /*点击了添加按钮*/
    private fun onAddItemClick(directionFlag: Int) {
        /*最大数量限制*/
        if (shapeViews.size >= MAX_COUNT) return
        addShape(directionFlag, true)

        addDeleteCallback?.shapeSizeChange(shapeViews.size)
    }

    fun setShapeData(points: ArrayList<ShapePosition>, ty: Type) {
        setShapeData(points, ty, true)
    }

    /*更新Type*/
    fun updateDataWithType(ty: Type, needResetCanvas: Boolean) {
        val data = getShapeRotations()
        setShapeData(data, ty, needResetCanvas)
    }

    /*设置图形数据，根据类型*/
    fun setShapeData(points: ArrayList<ShapePosition>, ty: Type, needUpdateCanvas: Boolean) {
        this.mType = ty
        reSetCanvas(false)
        canvasRotation = ((points.getOrNull(0)?.angle ?: 60f).toFloat()) - 60f
        val canvasCenterPoint = CANVAS_SIZE / 2f
        if (ty == Type.defaultType || ty == Type.edit || ty == Type.install || ty == Type.check || ty == Type.colorMode || ty == Type.focus) {
            val allShapeArea =
                floatArrayOf(canvasCenterPoint,
                    canvasCenterPoint,
                    canvasCenterPoint,
                    canvasCenterPoint)
            for (p in points) {
                if (p == null) continue
                /*添加图形，不更新其他view*/
                val areaPoint = addShapeWithAngle(p.angle, false)
                /*左右上下*/
                allShapeArea[0] = min(allShapeArea[0], areaPoint[0])
                allShapeArea[1] = max(allShapeArea[1], areaPoint[1])
                allShapeArea[2] = min(allShapeArea[2], areaPoint[2])
                allShapeArea[3] = max(allShapeArea[3], areaPoint[3])
            }

            if (ty == Type.check) {
                /*更新编号*/
                var num = 1
                shapeViews.forEach {
                    it.setNumText("${num++}")
                }
            }
            /*整体的宽高*/
            val shapeWidth = allShapeArea[1] - allShapeArea[0]
            val shapeHeight = allShapeArea[3] - allShapeArea[2]
            /*整个图形相对于画布中心的坐标*/
            val shapeOffsetPoint =
                PointF(-(allShapeArea[1] - shapeWidth / 2 - canvasCenterPoint),
                    -(allShapeArea[3] - shapeHeight / 2 - canvasCenterPoint))
            mShapeCenterPoint =
                PointF((allShapeArea[1] + allShapeArea[0]) / 2,
                    (allShapeArea[3] + allShapeArea[2]) / 2)
//            val shapeOffsetPoint = PointF(0f,  0f)
            /*整体相对于画布的拉伸*/
            if (needUpdateCanvas) {
                val scale = max(shapeWidth, shapeHeight) / CANVAS_SIZE
//                mDefaultScale = scale
                mDefaultOffsetPoint = shapeOffsetPoint
                callBack?.scaleAndTransition(scale, shapeOffsetPoint)
            }
            /*全部加完再更新UI*/
            updateUIState()
        }
    }


    /*获取最终传输给设备的旋转角度*/
    fun getShapeRotations(): ArrayList<ShapePosition> {
        val result = arrayListOf<ShapePosition>()

        shapeViews.forEach {
            result.add(it.toShapePosition())
        }

        return result
    }


    /*安装下一个方块 return true全部校验完了*/
    fun installNextShape(): SquraView? {
        if (mType != Type.install) return null

        val hasInstallNum = shapeViews.let { list ->
            var i = 0
            list.forEach { if (it.alpha >= 1) i++ }
            i
        }
        /*最后一个校验完*/
        if (hasInstallNum >= shapeViews.size) {
//            lastedShape?.change2InstalledState()
            return null
        }
        /*更新之前的View*/
        for (i in 0 until hasInstallNum) {
            shapeViews[i].change2InstalledState()
        }
        /*更新最后一个*/
        shapeViews[hasInstallNum].change2NextInstallState()
        pathViews.getOrNull(hasInstallNum + 1)?.change2InstalledState()

        val canvasCenterPoint = CANVAS_SIZE / 2f
        /*相对于画布中心的位置*/
        return shapeViews[hasInstallNum]
//        return shapeViews[hasInstallNum].pos?.let {
//            PointF(canvasCenterPoint - it.x, canvasCenterPoint - it.y)
//        }
//        return hasInstallNum >= shapeViews.size - 1
    }

    /*直接安装完所有方块*/
    fun installAllShape(): Boolean {
        if (mType != Type.install) return false
        /*更新之前的View*/
        for (i in 0 until shapeViews.size) {
            shapeViews[i].change2InstalledState()
        }
        return true
    }


    /*校准下一个方块 return null表示校准完*/
    fun checkNextShape(nextPos: Int): SquraView? {
        if (mType != Type.check) return null

        /*更新之前的View*/
        for (i in 0 until nextPos) {
            shapeViews.getOrNull(i)?.change2CheckedState()
        }
        shapeViews.getOrNull(nextPos)?.change2CheckingState()
        if (nextPos + 1 >= shapeViews.size - 1) return null

        for (i in nextPos + 1 until shapeViews.size) {
            shapeViews.getOrNull(i)?.change2UnCheckState(i+1)
        }
        /*相对于画布中心的位置*/
        return if(nextPos >= shapeViews.size - 1) null else shapeViews[nextPos]
    }

    /*校准完全部方块*/
    fun checkAllShape(): Boolean {
        if (mType != Type.check) return true

        /*更新之前的View*/
        for (i in 0 until shapeViews.size) {
            shapeViews.getOrNull(i)?.change2CheckedState()
        }
        return true
    }

    /*全选&取消全选*/
    fun setSelectAll(isSelectAll: Boolean): Boolean {
        if (mType == Type.colorMode) {
            shapeViews.forEach {
                it.mSelectted = isSelectAll
                it.invalidate()
            }
            return true
        }
        return false
    }

    /*设置颜色参数*/
    fun setColorParams(colorList: ArrayList<Int>,
                       brightnessList: ArrayList<Int>,
                       selectedList: ArrayList<Boolean>) {
        if (mType != Type.colorMode) return
        /*选中状态*/
        for (i in 0 until selectedList.size) {
            shapeViews.getOrNull(i)?.mSelectted = selectedList[i]
        }
        /*颜色,亮度*/
        val len4Color = minOf(colorList.size, shapeViews.size)
        for (i in 0 until len4Color) {
            shapeViews[i].setColorMode4Color(colorList[i])
        }
        val len4Brightness = minOf(brightnessList.size, shapeViews.size)
        for (i in 0 until len4Brightness) {
            shapeViews[i].setColorMode4Brightness(brightnessList[i])
        }

    }

    /*设置选点位置*/
    fun setSelectPoint(point: PointF?, isIn: Boolean) {
        this.isIn = isIn

        if (mType == Type.focus && point != null) {
            updateFocusDragItem(point)
        }
        else {
            updateDataWithType(Type.focus, false)
        }
    }


    /*获取已选中图形*/
    fun getSelectedShape(): ArrayList<ShapePosition> {
        val result = arrayListOf<ShapePosition>()
        shapeViews.forEach {
            if (it.mSelectted) {
                result.add(it.toShapePosition())
            }
        }
        return result
    }

    /*移动到默认位置*/
//    fun move2defaultPosition() {
//        if (mDefaultOffsetPoint != null) {
//            callBack?.scaleAndTransition(0f, mDefaultOffsetPoint!!)
//        }
//    }

    /*图形是否在画布上不可见，有图形可见 或没有图形数据或图形未绘制完返回false*/
    fun isShapeInvisible() : Boolean{
        val rect = Rect()
        shapeViews.forEach {
            if ( it.getGlobalVisibleRect(rect)) return false
        }
        return (shapeViews.size > 0 && !rect.isEmpty)
    }

    /*图形是否在画布上完全显示*/
    /* Rect.top 的值不为 0 时,View 要么部分可见,要么完全不可见*/
    fun isShapeAllVisible() : Boolean{
        val rect = Rect()
        shapeViews.forEach {
            if ( !it.getGlobalVisibleRect(rect)) return false
        }
        return (shapeViews.size > 0 && !rect.isEmpty)
    }

    /*更新拖动布局的缩放比例*/
    fun updateDragViewScale(scale : Float){
        if (scale <= 0) return
        mFocusDragViewScale = scale
        focusDragView?.scaleX = scale
        focusDragView?.scaleY = scale
    }

    /*重置画布,是否保留一个view*/
    fun reSetCanvas(keepOneView: Boolean) {
        removeAllViews()
        /*清空数据*/
        shapeViews = arrayListOf()
        addViews = arrayListOf()
        pathViews = arrayListOf()
        delView = null
        powerView = null
        focusDragView = null
        canvasRotation = 0f
        mShapeCenterPoint = null

        if (keepOneView) {
            val pointXY = CANVAS_SIZE / 2f
            val allShapeArea = floatArrayOf(pointXY, pointXY, pointXY, pointXY)
            val areaPoint = addShape(1, true)
            /*左右上下*/
            allShapeArea[0] = min(allShapeArea[0], areaPoint[0])
            allShapeArea[1] = max(allShapeArea[1], areaPoint[1])
            allShapeArea[2] = min(allShapeArea[2], areaPoint[2])
            allShapeArea[3] = max(allShapeArea[3], areaPoint[3])
            /*宽高*/
            val shapeWidth = allShapeArea[1] - allShapeArea[0]
            val shapeHeight = allShapeArea[3] - allShapeArea[2]
            /*整体相对于画布的拉伸*/
            val scale = max(shapeWidth, shapeHeight) / CANVAS_SIZE
            mDefaultOffsetPoint = PointF(0f, 0f)
            callBack?.scaleAndTransition(scale, PointF(0f, 0f))
        }

    }

    interface OnScaleAndTransitionChange {
        fun scaleAndTransition(scale: Float, point: PointF)
    }

    /*颜色模式下选择图形回调*/
    interface OnSelectShapeChange {
        fun selectedShape(selectedShapes: ArrayList<Boolean>)
    }

    /*聚焦模式下选择的点*/
    interface OnFocusDragChange {
        fun focusDragPointChange(point: PointF)
    }

    /*添加删除方块*/
    interface OnAddDeleteChange {
        fun shapeSizeChange(size : Int)
    }
}