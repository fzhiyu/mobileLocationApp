package com.example.mobilelocationapp.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class ChartService {
    private GraphicalView graphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset;//数据集容器
    private XYMultipleSeriesRenderer multipleSeriesRenderer;//渲染容器

    private XYSeries mSeries;//单条曲线数据集
    private XYSeriesRenderer mSeriesRenderer;//单条曲线渲染器

    private Context context;

    public ChartService(Context context) {
        this.context = context;
    }

    /**
     * 获取图表
     *
     * @return
     */
    public GraphicalView getGraphicalView() {
        graphicalView = ChartFactory.getCubeLineChartView(context,
                multipleSeriesDataset, multipleSeriesRenderer, 0.1f);
        return graphicalView;
    }

    /**
     * 获取数据集，及xy坐标的集合
     *
     * @param curveTitle
     */
    public void setMultipleSeriesDataset(String curveTitle) {//传入的有可能是数据的标识
        multipleSeriesDataset = new XYMultipleSeriesDataset();
        mSeries = new XYSeries(curveTitle);
        multipleSeriesDataset.addSeries(mSeries);//数据集给到数据集容器
    }

    /**
     * 获取渲染器容器
     *
     * @param maxX       x轴最大值
     * @param maxY       y轴最大值
     * @param chartTitle 曲线的标题
     * @param xTitle     x轴标题
     * @param yTitle     y轴标题
     * @param axeColor   坐标轴颜色
     * @param labelColor 标题颜色
     * @param curveColor 曲线颜色
     * @param gridColor  网格颜色
     */
    public void setMultipleSeriesRenderer(double maxX, double maxY,
                                          String chartTitle, String xTitle, String yTitle, int axeColor,
                                          int labelColor, int curveColor, int gridColor) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();

        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);//设置曲线标题
        }

        multipleSeriesRenderer.setXTitle(xTitle);
        multipleSeriesRenderer.setYTitle(yTitle);//设置X,Y轴的标题

        multipleSeriesRenderer.setLabelsColor(labelColor);//设置标题颜色

        //multipleSeriesRenderer.setRange(new double[]{0, maxX, -maxY, maxY});//xy轴的范围

        multipleSeriesRenderer.setXLabels(50);
        multipleSeriesRenderer.setYLabels(10);//设置X,Y轴的标记数

        multipleSeriesRenderer.setXLabelsAlign(Paint.Align.CENTER);
        multipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);//不知道是啥

        multipleSeriesRenderer.setAxisTitleTextSize(20);//设置轴文字大小
        multipleSeriesRenderer.setChartTitleTextSize(20);//设置图表标题大小
        multipleSeriesRenderer.setLabelsTextSize(20);//
        multipleSeriesRenderer.setLegendTextSize(20);//

        multipleSeriesRenderer.setPointSize(2f);//曲线描点尺寸

        multipleSeriesRenderer.setFitLegend(true);//设置合适的图例

        multipleSeriesRenderer.setMargins(new int[]{20, 30, 15, 20});//设定边距

        //multipleSeriesRenderer.setShowGrid(true);//显示网格
        multipleSeriesRenderer.setShowGridX(true);

        multipleSeriesRenderer.setZoomEnabled(true, false);//缩放功能，X运行，Y不允许
        multipleSeriesRenderer.setPanEnabled(true, false);//拖动功能, X,Y

        multipleSeriesRenderer.setAxesColor(axeColor);//设置坐标轴颜色

        multipleSeriesRenderer.setGridColor(gridColor);//设置网格颜色

        multipleSeriesRenderer.setBackgroundColor(Color.WHITE);//背景色

        multipleSeriesRenderer.setMarginsColor(Color.WHITE);//边距背景色，默认背景色为黑色，这里修改为白色

        mSeriesRenderer = new XYSeriesRenderer();//渲染器
        mSeriesRenderer.setColor(curveColor);//设置曲线颜色

        mSeriesRenderer.setPointStyle(PointStyle.CIRCLE);//描点风格，可以为圆点，方形点等等

        multipleSeriesRenderer.addSeriesRenderer(mSeriesRenderer);//将曲线渲染器放到渲染器容器中
    }

    /**
     * 根据新加的数据，更新曲线，只能运行在主线程
     *
     * @param x 新加点的x坐标
     * @param y 新加点的y坐标
     */
    public void updateChart(double x, double y) {
        mSeries.add(x, y);
        graphicalView.repaint();//此处也可以调用invalidate()
    }

    public void updateChart(List<Double> xList, List<Double> yList) {
        for (int i = 0; i < xList.size(); i++) {
            mSeries.add(xList.get(i), yList.get(i));
        }
        graphicalView.repaint();//此处也可以调用invalidate()
    }
}
