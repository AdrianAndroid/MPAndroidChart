package com.xxmassdeveloper.mpchartexample.flannery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class BezierCurveView extends View {
    private Paint paint;
    private Path path;
    private PointF startPoint;
    private PointF middlePoint;
    private PointF endPoint;
    private PointF controlPoint1;
    private PointF controlPoint2;

    public BezierCurveView(Context context) {
        super(context);
        init();
    }

    public BezierCurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierCurveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        path = new Path();

        startPoint = new PointF(100, 400);
        middlePoint = new PointF(100, 100);
        endPoint = new PointF(400, 100);

//        controlPoint1 = new PointF(250, 150);
//        controlPoint2 = new PointF(250, 250);
//        calculateControlPoints(controlPoint1, controlPoint2);
        // 计算斜率
        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float slope = dy / dx;
        // 计算中间线的角度(弧度)
        double angle = Math.atan(slope);
        // 计算偏移量
        float controlOffset = Math.abs(dx / 3f);
        // control位置
        float controlPoint1X = startPoint.x + controlOffset * (float) Math.cos(angle);
        float controlPoint1Y = startPoint.y + controlOffset * (float) Math.sin(angle);
        float controlPoint2X = endPoint.x - controlOffset * (float) Math.cos(angle);
        float controlPoint2Y = endPoint.y - controlOffset * (float) Math.sin(angle);
        controlPoint1 = new PointF(controlPoint1X, controlPoint1Y);
        controlPoint2 = new PointF(controlPoint2X, controlPoint2Y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path.reset();
        path.moveTo(startPoint.x, startPoint.y);
        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, endPoint.x, endPoint.y);

        canvas.drawPath(path, paint);

        canvas.drawCircle(startPoint.x, startPoint.y, 10f, paint);
        canvas.drawCircle(middlePoint.x, middlePoint.y, 10f, paint);
        canvas.drawCircle(endPoint.x, endPoint.y, 10f, paint);
    }

//    private void calculateControlPoints(PointF startPoint, PointF endPoint) {
//        float dx = endPoint.x - startPoint.x;
//        float dy = endPoint.y - startPoint.y;
//        float controlPoint1X = startPoint.x + dx / 3;
//        float controlPoint1Y = startPoint.y + dy / 3;
//        float controlPoint2X = endPoint.x - dx / 3;
//        float controlPoint2Y = endPoint.y - dy / 3;
//
//        controlPoint1.set(controlPoint1X, controlPoint1Y);
//        controlPoint2.set(controlPoint2X, controlPoint2Y);
//    }

}
