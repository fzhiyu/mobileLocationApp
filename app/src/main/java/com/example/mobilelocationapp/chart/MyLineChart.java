package com.example.mobilelocationapp.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MyLineChart {
    private LineChart mLineChart;//图表
    private String chartName;//图标的名称

    private XAxis mXAxis;
    private YAxis mYAxis;//x,y轴

    private Legend mLegend;//图例

    private LineData lineData = new LineData();//曲线集合的容器

    private ArrayList<LineDataSet> dataSets = new ArrayList<>();

    public MyLineChart(LineChart lineChart, String chartName) {
        this.mLineChart = lineChart;
        this.chartName = chartName;
    }

    public void initLineChart(){
        mXAxis = mLineChart.getXAxis();
        mYAxis = mLineChart.getAxisLeft();
        mLineChart.getAxisRight().setEnabled(false);//不显示右边的y轴

        mLegend = mLineChart.getLegend();//得到图例

        //设置图表的默认属性
        setLineChart(mLineChart, chartName);
        //设置XY轴的属性
        setXYAxis(mLineChart, mXAxis, mYAxis);

        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    private void setLineChart(LineChart lineChart, String chartName){
        Description description = new Description();  // 这部分是深度定制描述文本，颜色，字体等
        description.setText(chartName);
        description.setTextColor(Color.RED);
        lineChart.setDescription(description);

        lineChart.setNoDataText("暂无数据");//没有数据时样式

        //拖动和缩放
        lineChart.setDragEnabled(true);//可拖动
        lineChart.setScaleXEnabled(true);//x轴可缩放,y轴不可缩放
        lineChart.setScaleYEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);//不可双击缩放

        //高亮
        lineChart.setHighlightPerDragEnabled(false);//拖拽时不可高亮
        lineChart.setHighlightPerTapEnabled(false);//双击时不可高亮

        /***折线图例 标签 设置***/
        mLegend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        mLegend.setForm(Legend.LegendForm.LINE);
        mLegend.setTextSize(12f);
        //显示位置 左下方
        mLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        mLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        mLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        mLegend.setDrawInside(false);
    }

    private void setXYAxis(LineChart lineChart, XAxis xAxis, YAxis yAxis){
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴在下面

        xAxis.setDrawGridLines(false);// x轴不绘制网格线
        xAxis.setGranularity(1f);//设置最小间隔，防止当放大时，出现重复标签。
        //xAxis.setLabelCount(6, false);//设置X轴的刻度数量，第二个参数表示是否平均分配

        yAxis.setAxisMaximum(6);
        yAxis.setAxisMinimum(-6);
        yAxis.setDrawGridLines(false);// y轴不绘制网格线
        yAxis.setDrawZeroLine(true);//y轴原点处绘制一条线

        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return (int) value + "cm";
            }
        });
    }

    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode){
        lineDataSet.setColor(color);//曲线颜色
        lineDataSet.setDrawValues(false);//不显示值
        lineDataSet.setDrawCircles(false);//不显示数据点形状
        lineDataSet.setLineWidth(1f);//曲线宽度

        if (mode == null){//曲线形状, 默认为折线
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        }else {
            lineDataSet.setMode(mode);
        }
    }

    public void createLine(List<Entry> entryList, int color, LineDataSet.Mode mode, String label){
        LineDataSet dataSet = new LineDataSet(entryList, label);//一条曲线数据
        dataSets.add(dataSet);

        initLineDataSet(dataSet, color, mode);//设置曲线的属性

        if (lineData.getDataSets().size() == 0){
            float ratio = (float) entryList.size() / (float) 60;
            mLineChart.zoom(ratio, 1f, 0, 0);
        }

        lineData.addDataSet(dataSet);

        //通知数据已经改变
        lineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();

        //重绘
        mLineChart.invalidate();
    }

    public void clearLine(){
        for (LineDataSet dataSet: dataSets) {
            lineData.removeDataSet(dataSet);
        }
        //通知数据已经改变
        lineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();

        //重绘
        mLineChart.invalidate();

        dataSets.clear();
    }

}
