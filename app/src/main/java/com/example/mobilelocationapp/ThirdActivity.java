package com.example.mobilelocationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

import com.example.mobilelocationapp.chart.ChartService;

import org.achartengine.GraphicalView;

import java.util.Timer;
import java.util.TimerTask;

public class ThirdActivity extends AppCompatActivity {

    private LinearLayout lLayout_X_Error, lLayout_Y_Error;//存放表格的线性布局
    private GraphicalView XView, YView;//X，Y图表
    private ChartService XService, YService;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_layout);
    }

    public void initView(){
        //布局
        lLayout_X_Error = findViewById(R.id.llayout_X_error);
        lLayout_Y_Error = findViewById(R.id.llayout_Y_error);
    }

    public void setChart(){
        //误差曲线
        XService = new ChartService(this);
        XService.setMultipleSeriesDataset("X轴实时误差");
        XService.setMultipleSeriesRenderer(100, 100, "x轴实时误差", "时间", "误差",
                Color.RED, Color.RED, Color.RED, Color.BLACK);
        XView = XService.getGraphicalView();

        YService = new ChartService(this);
        YService.setMultipleSeriesDataset("Y轴实时误差");
        YService.setMultipleSeriesRenderer(100, 100, "y轴实时误差", "时间", "误差",
                Color.RED, Color.RED, Color.RED, Color.BLACK);
        YView = YService.getGraphicalView();

        lLayout_X_Error.addView(XView);
        lLayout_Y_Error.addView(YView);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 10, 1000);
    }

    private int t = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            XService.updateChart(t, Math.random() * 100);
            YService.updateChart(t, Math.random() * 100);
            t+=5;
        }
    };
}