package com.xxmassdeveloper.mpchartexample.flannery;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * https://blog.csdn.net/VickyTsai/article/details/90044515
 */
public class CustomLineChartRenderer extends LineChartRenderer {
    private final Path mRenderPath;

    public CustomLineChartRenderer(LineChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        PointF mFirstPointF = new PointF();
        PointF mLastPointF = new PointF();
        mRenderPath = new Path();
    }

    private float[] mLineBuffer = new float[4];

    /**
     * Draws a normal line.
     *
     * @param c
     * @param dataSet
     */
    protected void drawLinear(Canvas c, ILineDataSet dataSet) {
        int entryCount = dataSet.getEntryCount();
        final int pointsPerEntryPair = 2;
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        float phaseY = mAnimator.getPhaseY();
        mRenderPaint.setStyle(Paint.Style.STROKE);
        Canvas canvas = null;
        // if the data-set is dashed, draw on bitmap-canvas
        if (dataSet.isDashedLineEnabled()) {
            canvas = mBitmapCanvas;
        } else {
            canvas = c;
        }
        mXBounds.set(mChart, dataSet);
        // if drawing filled is enabled
        if (dataSet.isDrawFilledEnabled() && entryCount > 0) {
            drawLinearFill(c, dataSet, trans, mXBounds);
        }
        // more than 1 color
        if (dataSet.getColors().size() > 1) {
            int numberOfFloats = pointsPerEntryPair * 2;
            if (mLineBuffer.length <= numberOfFloats) {
                mLineBuffer = new float[numberOfFloats * 2];
            }
            int max = mXBounds.min + mXBounds.range;
            for (int j = mXBounds.min; j < max; j++) {
                Entry e = dataSet.getEntryForIndex(j);
                if (e == null) continue;
                mLineBuffer[0] = e.getX();
                mLineBuffer[1] = e.getY() * phaseY;
                if (j < mXBounds.max) {
                    e = dataSet.getEntryForIndex(j + 1);
                    if (e == null) break;
                    mLineBuffer[2] = e.getX();
                    mLineBuffer[3] = e.getY() * phaseY;
                } else {
                    mLineBuffer[2] = mLineBuffer[0];
                    mLineBuffer[3] = mLineBuffer[1];
                }
                // Determine the start and end coordinates of the line, and make sure they differ.
                float firstCoordinateX = mLineBuffer[0];
                float firstCoordinateY = mLineBuffer[1];
                float lastCoordinateX = mLineBuffer[numberOfFloats - 2];
                float lastCoordinateY = mLineBuffer[numberOfFloats - 1];

                if (firstCoordinateX == lastCoordinateX && firstCoordinateY == lastCoordinateY) {
                    continue;
                }
                trans.pointValuesToPixel(mLineBuffer);
                if (!mViewPortHandler.isInBoundsRight(firstCoordinateX)) {
                    break;
                }
                // make sure the lines don't do shitty things outside
                // bounds
                if (!mViewPortHandler.isInBoundsLeft(lastCoordinateX) ||
                        !mViewPortHandler.isInBoundsTop(Math.max(firstCoordinateY, lastCoordinateY)) ||
                        !mViewPortHandler.isInBoundsBottom(Math.min(firstCoordinateY, lastCoordinateY))) {
                    continue;
                }
                // get the color that is set for this line-segment
                mRenderPaint.setColor(dataSet.getColor(j));
                canvas.drawLines(mLineBuffer, 0, pointsPerEntryPair * 2, mRenderPaint);
            }

        } else { // only one color per dataset
            if (mLineBuffer.length < Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 2) {
                mLineBuffer = new float[Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 4];
            }
            Entry e1, e2;
            e1 = dataSet.getEntryForIndex(mXBounds.min); // 获取第一个点
            if (e1 != null) {
                int j = 0;
                int startX = mXBounds.min;
                int endX = mXBounds.range + mXBounds.min;
                int indexX = startX;
                while (indexX <= endX) {
                    if (indexX == 0) {
                        e1 = dataSet.getEntryForIndex(0);
                    } else {
                        do {
                            e1 = dataSet.getEntryForIndex(indexX == 0 ? 0 : (indexX - 1));
                            if (e1 != null && !e1.isValid()) {
                                indexX++;
                            }
                        } while (e1 != null && !e1.isValid() && indexX <= endX);
                    }
                    do {
                        e2 = dataSet.getEntryForIndex(indexX);
                        if (e2 != null && !e2.isValid()) {
                            indexX++;
                        }
                    } while (e2 != null && !e2.isValid() && indexX <= endX);
                    if (e1 == null || e2 == null) continue;
                    mLineBuffer[j++] = e1.getX();
                    mLineBuffer[j++] = e1.getY() * phaseY;
                    mLineBuffer[j++] = e2.getX();
                    mLineBuffer[j++] = e2.getY() * phaseY;
                    indexX++;
                }
                if (j > 0) {
                    trans.pointValuesToPixel(mLineBuffer); // 显示文字
                    final int size = Math.max((mXBounds.range + 1) * pointsPerEntryPair, pointsPerEntryPair) * 2;
                    mRenderPaint.setColor(dataSet.getColor());
                    canvas.drawLines(mLineBuffer, 0, size, mRenderPaint);
                }
            }
        }
        mRenderPaint.setPathEffect(null);
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

    @Override
    protected void drawCircles(Canvas c) {
        mRenderPaint.setStyle(Paint.Style.FILL);
        float phaseY = mAnimator.getPhaseY();
        mCirclesBuffer[0] = 0;
        mCirclesBuffer[1] = 0;
        List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();
        for (int i = 0; i < dataSets.size(); i++) {
            ILineDataSet dataSet = dataSets.get(i);
            if (!dataSet.isVisible() || !dataSet.isDrawCirclesEnabled() || dataSet.getEntryCount() == 0) {
                continue;
            }
            mCirclePaintInner.setColor(dataSet.getCircleHoleColor());
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            mXBounds.set(mChart, dataSet);
            float circleRadius = dataSet.getCircleRadius();
            float circleHoleRadius = dataSet.getCircleHoleRadius();
            boolean drawCircleHole = dataSet.isDrawCircleHoleEnabled() && circleHoleRadius < circleRadius && circleHoleRadius > 0.f;
            boolean drawTransparentCircleHole = drawCircleHole && dataSet.getCircleHoleColor() == ColorTemplate.COLOR_NONE;

            DataSetImageCache imageCache;

            if (mImageCaches.containsKey(dataSet)) {
                imageCache = mImageCaches.get(dataSet);
            } else {
                imageCache = new DataSetImageCache();
                mImageCaches.put(dataSet, imageCache);
            }

            boolean changeRequired = imageCache.init(dataSet);

            // only fill the cache with new bitmaps if a change is required
            if (changeRequired) {
                imageCache.fill(dataSet, drawCircleHole, drawTransparentCircleHole);
            }

            int boundsRangeCount = mXBounds.range + mXBounds.min;

            for (int j = mXBounds.min; j <= boundsRangeCount; j++) {

                Entry e = dataSet.getEntryForIndex(j);

                if (e == null) break;
                if (!e.isValid()) {
                    continue;
                }

                mCirclesBuffer[0] = e.getX();
                mCirclesBuffer[1] = e.getY() * phaseY;

                trans.pointValuesToPixel(mCirclesBuffer);

                if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0])) {
                    break;
                }

                if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) || !mViewPortHandler.isInBoundsY(mCirclesBuffer[1])) {
                    continue;
                }
                Bitmap circleBitmap = imageCache.getBitmap(j);

                if (circleBitmap != null) {
                    c.drawBitmap(circleBitmap, mCirclesBuffer[0] - circleRadius, mCirclesBuffer[1] - circleRadius, null);
                }
            }
        }
    }
}