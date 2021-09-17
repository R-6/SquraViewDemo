package com.example.squraviewdemo;

/**
 * Create by lvwenrui on 2021/7/2 15:42
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.Nullable;

public class MoveAndCropRectView extends View {
    // 绘制 损害框和损害名称
    private Paint mPaint;
    private RectF mRectF;
    // 边缘字体
// private BorderedText mBorderedText;
// 标题 或 名字
    private String mTitle;
    // 概率
    private float mConfidence;
    // 矩形框 corner 的角度：直角、圆角
    private int mCornerAngle;
    //直角 默认
    public static final int RIGHT_CORNER = 0;
    //圆角
    public static final int ROUND_CORNER = 1;
    // Remove Rect
    private int MODE;
    private static final int MODE_OUTSIDE = 0x000000aa;/*170*/
    private static final int MODE_INSIDE = 0x000000bb;/*187*/
    private static final int MODE_POINT = 0X000000cc;/*204*/
    private static final int MODE_ILLEGAL = 0X000000dd;/*221*/
    private float startX;/*start X location*/
    private float startY;/*start Y location*/
    private float endX;/*end X location*/
    private float endY;/*end Y location*/
    private float currentX;/*X coordinate values while finger press*/
    private float currentY;/*Y coordinate values while finger press*/
    private float memoryX;/*the last time the coordinate values of X*/
    private float memoryY;/*the last time the coordinate values of Y*/
    private float mCoverWidth;/*width of selection box*/
    private float mCoverHeight;/*height of selection box*/
    private static final int ACCURACY = 100;/*touch accuracy*/
    private int pointPosition;/*vertex of a rectangle*/
    private static final float minWidth = 100.0f;/*the minimum width of the rectangle*/
    private static final float minHeight = 200.0f;/*the minimum height of the rectangle*/
    private onLocationListener mLocationListener;/*listen to the Rect */
    private static final float EDGE_WIDTH = 1.8f;

    public MoveAndCropRectView(Context context) {
        this(context, null);
    }

    public MoveAndCropRectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoveAndCropRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDatas(context);
    }

    private void initDatas(Context context) {
        mPaint = new Paint();
        mRectF = new RectF();
//画笔设置空心
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
//  float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//    18.0f, context.getResources().getDisplayMetrics());
//  mBorderedText = new BorderedText(textSizePx);
        currentX = 0;
        currentY = 0;
    }

    private boolean firstDraw = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//  switch (mCornerAngle) {
//   case RIGHT_CORNER:// 绘制 损害框(直角矩形框)
//    drawRect(canvas);
//    break;
//   case ROUND_CORNER:// 绘制 损害框(圆角矩形框)
//    drawRoundRect(canvas);
//    break;
//  }
        if (firstDraw) {
            firstDraw = false;
            startX = mRectF.left;
            startY = mRectF.top;
            endX = mRectF.right;
            endY = mRectF.bottom;
            mCoverWidth = mRectF.width();
            mCoverHeight = mRectF.height();
        }
        if (mLocationListener != null) {
            mLocationListener.locationRect(startX, startY, endX, endY);
        }
//  LogUtils.d("onDraw -- startX: " + startX);
        canvas.drawLine(startX - EDGE_WIDTH, startY - EDGE_WIDTH,
                endX + EDGE_WIDTH, startY - EDGE_WIDTH, mPaint);/*top 上边框-*/
        canvas.drawLine(startX - EDGE_WIDTH, endY + EDGE_WIDTH,
                endX + EDGE_WIDTH, endY + EDGE_WIDTH, mPaint);/*bottom -*/
        canvas.drawLine(startX - EDGE_WIDTH, startY - EDGE_WIDTH,
                startX - EDGE_WIDTH, endY + EDGE_WIDTH, mPaint);/*left |*/
        canvas.drawLine(endX + EDGE_WIDTH, startY - EDGE_WIDTH,
                endX + EDGE_WIDTH, endY + EDGE_WIDTH, mPaint);/*right |*/
// 绘制名称 和 概率
//  final String labelString =
//    !TextUtils.isEmpty(mTitle)
//      ? String.format("%s %.2f", mTitle, (100 * mConfidence))
//      : String.format("%.2f", (100 * mConfidence));
//
//  // 在 直角矩形框 上写字
//  mBorderedText.drawText(canvas,
//    startX,
//    startY, labelString + "%",
//    mPaint);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                memoryX = event.getX();
                memoryY = event.getY();
                checkMode(memoryX, memoryY);
                break;
            case MotionEvent.ACTION_MOVE: {
                currentX = event.getX();
                currentY = event.getY();
                switch (MODE) {
                    case MODE_ILLEGAL:
                        recoverFromIllegal(currentX, currentY);
                        postInvalidate();
                        break;
                    case MODE_OUTSIDE:
//do nothing;
                        break;
                    case MODE_INSIDE://拖动
                        moveByTouch(currentX, currentY);
                        postInvalidate();
                        break;
                    default:
                        /*MODE_POINT*/
                        moveByPoint(currentX, currentY);
                        postInvalidate();
                        break;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
//    mPaint.setColor(getContext().getResources().getColor(R.color.orange));
                postInvalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /*点击顶点附近时的缩放处理*/
    @SuppressWarnings("SuspiciousNameCombination")
    private void moveByPoint(float bx, float by) {
//  LogUtils.d("moveByPoint");
        switch (pointPosition) {
            case 0:/*left-up*/
                mCoverWidth = Math.abs(endX - bx);
                mCoverHeight = Math.abs(endY - by);
//noinspection SuspiciousNameCombination
                if (!checkLegalRect(mCoverWidth, mCoverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    refreshLocation(bx, by, endX, endY);
                }
                break;
            case 1:/*right-up*/
                mCoverWidth = Math.abs(bx - startX);
                mCoverHeight = Math.abs(endY - by);
                if (!checkLegalRect(mCoverWidth, mCoverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    refreshLocation(startX, by, bx, endY);
                }
                break;
            case 2:/*left-down*/
                mCoverWidth = Math.abs(endX - bx);
                mCoverHeight = Math.abs(by - startY);
                if (!checkLegalRect(mCoverWidth, mCoverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    refreshLocation(bx, startY, endX, by);
                }
                break;
            case 3:/*right-down*/
                mCoverWidth = Math.abs(bx - startX);
                mCoverHeight = Math.abs(by - startY);
                if (!checkLegalRect(mCoverWidth, mCoverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    refreshLocation(startX, startY, bx, by);
                }
                break;
            default:
                break;
        }
    }

    /*刷新矩形的坐标*/
    private void refreshLocation(float isx, float isy, float iex, float iey) {
        this.startX = isx;
        this.startY = isy;
        this.endX = iex;
        this.endY = iey;
        mCoverWidth = endX - startX;
        mCoverHeight = endY - startY;
    }

    /*检测矩形是否达到最小值*/
    private boolean checkLegalRect(float cHeight, float cWidth) {
        return (cHeight < minHeight && cWidth <minWidth);
    }

    /*从非法状态恢复，这里处理的是达到最小值后能拉伸放大*/
    private void recoverFromIllegal(float rx, float ry) {
        if ((rx > startX && ry> startY) &&(rx < endX && ry < endY)){
            MODE = MODE_ILLEGAL;
        } else{
            MODE = MODE_POINT;
        }
    }

    /**
     * 判断点在矩形的什么位置
     *
     * @param cx
     * @param cy
     */
    private void checkMode(float cx, float cy) {
        if (cx > startX && cx < endX && cy > startY && cy < endY){
            MODE = MODE_INSIDE;//矩形内部
        } else if (nearbyPoint(cx, cy) < 4) {
            MODE = MODE_POINT;//矩形点上
        } else {
            MODE = MODE_OUTSIDE;//矩形外部
        }
    }

    /*矩形随手指移动*/
    private void moveByTouch(float mx, float my) {/*move center point*/
        float dX = mx - memoryX;
        float dY = my - memoryY;
        startX += dX;
        startY += dY;
        if (startX <= 0) {
            startX = 0;
        }
        if (startY <= 0) {
            startY = 0;
        }
        endX = startX + mCoverWidth;
        endY = startY + mCoverHeight;
        if (endX == 1920) {
            endX = 1920;
            startX = endX - mCoverWidth;
        }
        if (endY == 1080) {
            endY = 1080;
            startY = endY - mCoverHeight;
        }
        memoryX = mx;
        memoryY = my;
    }

    /*判断点(inX,inY)是否靠近矩形的4个顶点*/
    private int nearbyPoint(float floatX, float floatY) {
        if ((Math.abs(startX - floatX) <= ACCURACY && (Math.abs(floatY - startY) <= ACCURACY))) {/*left-up angle*/
            pointPosition = 0;
            return 0;
        }
        if ((Math.abs(endX - floatX) <= ACCURACY && (Math.abs(floatY - startY) <= ACCURACY))) {/*right-up angle*/
            pointPosition = 1;
            return 1;
        }
        if ((Math.abs(startX - floatX) <= ACCURACY && (Math.abs(floatY - endY) <= ACCURACY))) {/*left-down angle*/
            pointPosition = 2;
            return 2;
        }
        if ((Math.abs(endX - floatX) <= ACCURACY && (Math.abs(floatY - endY) <= ACCURACY))) {/*right-down angle*/
            pointPosition = 3;
            return 3;
        }
        pointPosition = 100;
        return 100;
    }

    // 设置矩形框
    public void setRectF(RectF rectf) {
        this.mRectF = rectf;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setConfidence(float confidence) {
        mConfidence = confidence;
    }

    public void setCornerAngle(int cornerAngle) {
        this.mCornerAngle = cornerAngle;
    }

    // 绘制 损害框(直角矩形框)
    private void drawRect(Canvas canvas) {
        canvas.drawRect(mRectF, mPaint);
// 绘制名称 和 概率
//  final String labelString =
//    !TextUtils.isEmpty(mTitle)
//      ? String.format("%s %.2f", mTitle, (100 * mConfidence))
//      : String.format("%.2f", (100 * mConfidence));
// 在 直角矩形框 上写字
//  mBorderedText.drawText(canvas,
//    mRectF.left,
//    mRectF.top, labelString + "%",
//    mPaint);
    }

    // 绘制 损害框(圆角矩形框)
    private void drawRoundRect(Canvas canvas) {
        float cornerSize = Math.min(mRectF.width(), mRectF.height()) / 8.0f;
        canvas.drawRoundRect(mRectF, cornerSize, cornerSize, mPaint);
// 绘制名称 和 概率
//  final String labelString =
//    !TextUtils.isEmpty(mTitle)
//      ? String.format("%s %.2f", mTitle, (100 * mConfidence))
//      : String.format("%.2f", (100 * mConfidence));
// 在 圆角矩形框 上写字
//  mBorderedText.drawText(canvas,
//    mRectF.left + cornerSize,
//    mRectF.top, labelString + "%",
//    mPaint);
    }

    public void setLocationListener(onLocationListener mLocationListener) {
        this.mLocationListener = mLocationListener;
    }

    public interface onLocationListener {
        void locationRect(float startX, float startY, float endX, float endY);
    }
}