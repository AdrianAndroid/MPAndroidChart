package com.xxmassdeveloper.mpchartexample.flannery;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * https://blog.csdn.net/VickyTsai/article/details/90044515
 */
public class CustomLineChartRenderer extends LineChartRenderer {
    private final PointF mFirstPointF;
    private final PointF mLastPointF;
    private final Path mRenderPath;

    public CustomLineChartRenderer(LineChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        mFirstPointF = new PointF();
        mLastPointF = new PointF();
        mRenderPath = new Path();
    }

    @Override
    protected void drawLinear(Canvas c, ILineDataSet dataSet) {
        int entryCount = dataSet.getEntryCount();

        if (entryCount < 1)
            return;

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mXBounds.set(mChart, dataSet);

        Entry entryFirst = dataSet.getEntryForIndex(mXBounds.min);
        Entry entryLast = dataSet.getEntryForIndex(mXBounds.max);

        int minx = Math.max(mXBounds.min, 0);
        int maxx = Math.min(mXBounds.max + 1, entryCount);

        // 遍历数据点并绘制线条
        for (int j = minx; j < maxx; j++) {
            Entry entry = dataSet.getEntryForIndex(j);
            if (entry == null)
                continue;

            float x = entry.getX();
            float y = entry.getY();

            if (Float.isNaN(y) || y < 0) {
                if (mRenderPath.isEmpty()) {
                    mFirstPointF.x = x;
                    mFirstPointF.y = y;
                    mRenderPath.moveTo(mFirstPointF.x, mFirstPointF.y);
                } else {
                    mLastPointF.x = x;
                    mLastPointF.y = y;
                    mRenderPath.lineTo(mLastPointF.x, mLastPointF.y);
                    c.drawPath(mRenderPath, mRenderPaint);
                    mRenderPath.reset();
                    mRenderPath.moveTo(mLastPointF.x, mLastPointF.y);
                }
            } else {
                if (mRenderPath.isEmpty()) {
                    mFirstPointF.x = x;
                    mFirstPointF.y = y;
                    mRenderPath.moveTo(mFirstPointF.x, mFirstPointF.y);
                } else {
                    mLastPointF.x = x;
                    mLastPointF.y = y;
                    mRenderPath.lineTo(mLastPointF.x, mLastPointF.y);
                }
            }
        }

        if (!mRenderPath.isEmpty()) {
            c.drawPath(mRenderPath, mRenderPaint);
            mRenderPath.reset();
        }
    }

    @Override
    protected void drawCubicBezier(ILineDataSet dataSet) {
//        super.drawCubicBezier(dataSet);
        drawCubicBezier2(dataSet);
        if (true) {
            return;
        }
        float phaseY = mAnimator.getPhaseY();
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency()); // 转化文字标题
        mXBounds.set(mChart, dataSet);
        float intensity = dataSet.getCubicIntensity();
        cubicPath.reset();
        if (mXBounds.range >= 1) {
            float prevDx = 0f;
            float prevDy = 0f;
            float curDx = 0f;
            float curDy = 0f;
            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1
            final int firstIndex = mXBounds.min + 1;
            final int lastIndex = mXBounds.min + mXBounds.range;

            Entry prevPrev;
            Entry prev = dataSet.getEntryForIndex(Math.max(firstIndex - 2, 0)); // 上一个点
            Entry cur = dataSet.getEntryForIndex(Math.max(firstIndex - 1, 0)); // 当前点
            Entry next = cur;
            int nextIndex = -1;
            if (cur == null || !cur.isValid()) return;
            // let the spline start
            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);
            for (int jIndex = (mXBounds.min + 1); jIndex <= (mXBounds.range + mXBounds.min); jIndex++) {
                prevPrev = prev;
                prev = cur;
                cur = nextIndex == jIndex ? next : dataSet.getEntryForIndex(jIndex);

                nextIndex = jIndex + 1 < dataSet.getEntryCount() ? jIndex + 1 : jIndex;
                next = dataSet.getEntryForIndex(nextIndex);

                prevDx = (cur.getX() - prevPrev.getX()) * intensity;
                prevDy = (cur.getY() - prevPrev.getY()) * intensity;
                curDx = (next.getX() - prev.getX()) * intensity;
                curDy = (next.getY() - prev.getY()) * intensity;

                float x1 = prev.getX() + prevDx; // 第一个控制点的x坐标
                float y1 = (prev.getY() + prevDy) * phaseY; // 第一个点的y坐标
                float x2 = cur.getX() - curDx; // 第二个控制点的x坐标
                float y2 = (cur.getY() - curDy) * phaseY; // 第二个控制点的y坐标
                float x3 = cur.getX(); // 端点的x坐标
                float y3 = cur.getY() * phaseY; // 端点的y坐标

                /*
                 * 从最后一个点开始添加一个三次贝塞尔，接近控制点（x1，y1）和（x2，y2），并在（x3，y3）处结束。如果未对此轮廓进行moveTo（）调用，则第一个点将自动设置为（0,0）。
                 *
                 * @param x1 三次曲线上第一个控制点的x坐标
                 * @param y1 三次曲线上第一个控制点的y坐标
                 * @param x2 三次曲线上第二个控制点的x坐标
                 * @param y2 三次曲线上第二个控制点的y坐标
                 * @param x3 三次曲线端点的x坐标
                 * @param y3 三次曲线端点的y坐标
                 */
                cubicPath.cubicTo(x1, y1, x2, y2, x3, y3);
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled()) {
            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);
            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());
        mRenderPaint.setStyle(Paint.Style.STROKE);
        trans.pathValueToPixel(cubicPath);
        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);
        mRenderPaint.setPathEffect(null);
    }

    protected void drawCubicBezier2(ILineDataSet dataSet) {
//        super.drawCubicBezier(dataSet);
        float phaseY = mAnimator.getPhaseY();
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency()); // 转化文字标题
        mXBounds.set(mChart, dataSet);
        float intensity = dataSet.getCubicIntensity();
        cubicPath.reset();
        if (mXBounds.range >= 1) {
            float prevDx = 0f;
            float prevDy = 0f;
            float curDx = 0f;
            float curDy = 0f;
            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1
            final int firstIndex = mXBounds.min + 1;
            final int lastIndex = mXBounds.min + mXBounds.range;

            Entry prevPrev;
            Entry prev = dataSet.getEntryForIndex(Math.max(firstIndex - 2, 0)); // 上一个点
            Entry cur = dataSet.getEntryForIndex(Math.max(firstIndex - 1, 0)); // 当前点
            Entry next = cur;
            int nextIndex = -1;
            if (cur == null) return;
            // let the spline start
            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);
            int jIndex = (mXBounds.min + 1);
            int endIndex = mXBounds.range + mXBounds.min;
            while (jIndex <= endIndex) {
                prevPrev = prev;
                do {
                    prev = cur;
                    cur = dataSet.getEntryForIndex(jIndex);
                    if (!cur.isValid()) {
                        jIndex++;
                    }
                } while (!cur.isValid() && jIndex <= endIndex);

                if (jIndex > endIndex) {
                    next = cur;
                } else {
                    do {
                        // nextIndex = jIndex + 1 < dataSet.getEntryCount() ? jIndex + 1 : jIndex;
                        next = dataSet.getEntryForIndex(jIndex);
                        if (!next.isValid()) {
                            jIndex++;
                        }
                    } while(!next.isValid() && jIndex <= endIndex);
                }

                prevDx = (cur.getX() - prevPrev.getX()) * intensity;
                prevDy = (cur.getY() - prevPrev.getY()) * intensity;
                curDx = (next.getX() - prev.getX()) * intensity;
                curDy = (next.getY() - prev.getY()) * intensity;

                float x1 = prev.getX() + prevDx; // 第一个控制点的x坐标
                float y1 = (prev.getY() + prevDy) * phaseY; // 第一个点的y坐标
                float x2 = cur.getX() - curDx; // 第二个控制点的x坐标
                float y2 = (cur.getY() - curDy) * phaseY; // 第二个控制点的y坐标
                float x3 = cur.getX(); // 端点的x坐标
                float y3 = cur.getY() * phaseY; // 端点的y坐标

                /*
                 * 从最后一个点开始添加一个三次贝塞尔，接近控制点（x1，y1）和（x2，y2），并在（x3，y3）处结束。如果未对此轮廓进行moveTo（）调用，则第一个点将自动设置为（0,0）。
                 *
                 * @param x1 三次曲线上第一个控制点的x坐标
                 * @param y1 三次曲线上第一个控制点的y坐标
                 * @param x2 三次曲线上第二个控制点的x坐标
                 * @param y2 三次曲线上第二个控制点的y坐标
                 * @param x3 三次曲线端点的x坐标
                 * @param y3 三次曲线端点的y坐标
                 */
                cubicPath.cubicTo(x1, y1, x2, y2, x3, y3);

                jIndex++;
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled()) {
            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);
            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());
        mRenderPaint.setStyle(Paint.Style.STROKE);
        trans.pathValueToPixel(cubicPath);
        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);
        mRenderPaint.setPathEffect(null);
    }
}