package com.example.mobilelocationapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mobilelocationapp.chart.ChartService;
import com.example.mobilelocationapp.fzy.MainActivity2;
import com.example.mobilelocationapp.fzy.MyService;

import org.achartengine.GraphicalView;

import java.util.Timer;
import java.util.TimerTask;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{

    public static String startSystem = "StartSystem\\r\\n";
    public static String stopSystem = "StopSystem\\r\\n";
    private boolean isSystemOn = false;

    private LinearLayout lLayout_X_Error, lLayout_Y_Error;//存放表格的线性布局
    private GraphicalView XView, YView;//X，Y图表
    private ChartService XService, YService;
    private Timer timer;

    private RelativeLayout rLayout;
    private CarErrorView carErrorView;

    private float[] carsDistance;//小车的距离
    private float[] carsAngle;//小车的角度
    private int carsNumber;//小车的数量

    private Button offOnSystem, stopEmergency;

    volatile MyService.MyBinder myBinder;
    MyService myService;
    Boolean myBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.second_layout);

        //设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //设置输入法不自动弹出
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = new Intent(SecondActivity.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 在此处添加执行的代码
                if (myBinder != null) {
                    myBinder.sendMessageBind("test");

                }
                Log.e(TAG, "run: handler;");
                handler.removeCallbacks(this);
            }
        };
        handler.postDelayed(runnable, 1000);// 打开定时器，50ms后执行runnable操作

        //按钮点击
        offOnSystem = findViewById(R.id.btn_off_on_system);
        stopEmergency = findViewById(R.id.btn_stop_emergency);

        offOnSystem.setOnClickListener(this);
        stopEmergency.setOnClickListener(this);

        //布局
        lLayout_X_Error = findViewById(R.id.llayout_X_error);
        lLayout_Y_Error = findViewById(R.id.llayout_Y_error);
        rLayout = findViewById(R.id.rLayout);

        carsDistance = new float[]{250, 250, 250, 250};
        carsAngle = new float[]{45, 135, 225, 315};
        carErrorView = new CarErrorView(this, null, carsDistance, carsAngle);
        rLayout.addView(carErrorView);

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

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();
            myBound = true;
            Log.e(TAG, "second: 绑定" );
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myBound = false;
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_off_on_system:
                isSystemOn = !isSystemOn;
                if (isSystemOn){
                    Toast.makeText(this, startSystem, Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(this, stopSystem, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_stop_emergency:
                //紧急停止
                break;

        }
    }
}