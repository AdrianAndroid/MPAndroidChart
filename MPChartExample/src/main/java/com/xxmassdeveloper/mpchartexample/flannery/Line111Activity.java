package com.xxmassdeveloper.mpchartexample.flannery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.xxmassdeveloper.mpchartexample.R;

import java.util.ArrayList;
import java.util.List;

public class Line111Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line111);
        final LineChart lineChart = findViewById(R.id.lineChart);
        initLineChart(lineChart);

//        lineChart.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setData(lineChart);
//            }
//        }, 1000);


        setData(lineChart, new Float[]{10f, 25f, 30f, 50f, 40f, 35f, 80f});
        findViewById(R.id.btnClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(lineChart, new Float[]{10f, 25f, 30f, 50f, 40f, 35f, 80f});
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(lineChart, new Float[]{50f, 0f, 0f, 0f, 0f, 0f, 0f});
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(lineChart, new Float[]{0f, 0f, 0f, 0f, 0f, 0f, 50f});
            }
        });
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(lineChart, new Float[]{50f, 0f, 0f, 40f, 60f, 50f, 40f});
            }
        });
        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(lineChart, new Float[]{50f, 0f, 0f, 40f, 0f, 0f, 40f});
            }
        });
    }

    void initLineChart(LineChart lineChart) {
        // lineChart.setViewPortOffsets(10f, 10f, 10f, 10f)
        lineChart.setNoDataText("");
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
//        lineChart.background = ContextCompat.getDrawable(globalApp, R.drawable.shape_trend_5_line_background)
        lineChart.setMaxHighlightDistance(126f);
        lineChart.getAxisLeft().setStartAtZero(true);
        XAxis x = lineChart.getXAxis();
        x.setEnabled(false);
        x.setDrawAxisLine(false);

        YAxis y = lineChart.getAxisLeft();
        y.setLabelCount(5, true);
        y.setEnabled(true);
        y.enableGridDashedLine(10f, 10f, 0f);
        y.setGridColor(Color.parseColor("#33ffffff"));
        y.setGridLineWidth(1f);
        y.setDrawGridLines(true);
        y.setDrawAxisLine(false);
        y.setDrawLabels(false);
        //y.setValueFormatter { value, _ -> value.toString() }dd
//        lineChart.renderer = CustomLineChartRenderer(lineChart, lineChart.animator, lineChart.viewPortHandler)
        lineChart.setRenderer(new CustomLineChartRenderer(lineChart, lineChart.getAnimator(), lineChart.getViewPortHandler()));

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        //lineChart.animateXY(1000, 1000)
    }

    void setData(LineChart lineChart, Float[] floatValues) {
        // 0, 80, 0, 20, 20, 40, 10
        List<Entry> values = new ArrayList<>();
        List<Integer> colorsList = new ArrayList<Integer>();
        for (int i = 0; i < floatValues.length; i++) {
            float v = floatValues[i];
            if (v == 0f) {
                colorsList.add(Color.TRANSPARENT);
            } else {
                colorsList.add(Color.rgb(125, 155, 255));
            }
            values.add(new Entry((float) i, v, v != 0f));
        }

        lineChart.getAxisLeft().setAxisMaximum(80);
        lineChart.getAxisLeft().setAxisMinimum(0);
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        } else {
            LineDataSet set = new LineDataSet(values, "");
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setCubicIntensity(0.5f);
            set.setDrawFilled(true);
            //set.setDrawCircles(true) // 肯定是坐标上的小点
            set.setLineWidth(0.8f);
            set.setCircleRadius(4f);
            set.setCircleColors(colorsList);
            //set.setCircleColor(Color.rgb(125, 155, 255))
            set.setDrawCircleHole(false);
            set.setColor(Color.parseColor("#7D9BFF"));
            set.setFillColor(Color.parseColor("#7D9BFF"));
            //val drawable = ContextCompat.getDrawable(globalApp, R.drawable.shape_trend_gradient_9288ff)
            //set.fillDrawable = drawable
            set.setDrawHorizontalHighlightIndicator(false);
            set.setColor(Color.GREEN);
//            LineDataSet set2 = LineDataSet(listOf(Entry(0f, max)), "")
//            set2.setDrawFilled(false)
//            set2.setCircleColor(Color.TRANSPARENT)
//            set2.setDrawValues(false)
//            set2.setDrawCircleHole(false)
            LineData lineChartData = new LineData(set);
            lineChartData.setValueTextSize(9f);
            lineChartData.setDrawValues(false);
            lineChart.setData(lineChartData);
        }
        lineChart.invalidate();
    }
}