package com.example.mobilelocationapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilelocationapp.chart.ChartService;
import com.example.mobilelocationapp.fzy.MainActivity2;
import com.example.mobilelocationapp.fzy.MyService;

import org.achartengine.GraphicalView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SecondActivity extends AppCompatActivity{

    public static final String ENTER = "\r\n";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";
    public static final String StOP = "STOP";
    public static final String StOPEM = "STOP 0";
    public static final String ROTATEL = "ROTATEL";//旋转
    public static final String ROTATER = "ROTATER";
    public static final String STARTSYSTEM = "StartSystem";//开启关闭跟踪系统
    public static final String STOPSYSTEM = "StopSystem";
    public static final String STOPCAR = "StopCar";//停止从运动平台

    private LinearLayout lLayout_X_Error, lLayout_Y_Error;//存放表格的线性布局
    private GraphicalView XView, YView;//X，Y图表
    private ChartService XService, YService;
    private Timer timer;

    private RelativeLayout rLayout;
    private CarErrorView carErrorView;

    private float[] carsDistance;//小车的距离
    private float[] carsAngle;//小车的角度
    private int carsNumber;//小车的数量

    private SeekBar seekBar_up, seekBar_down, seekBar_time;//拖动条
    private TextView tv_up, tv_down, tv_time;
    private double up_acc = 1, down_acc = 1, speed = 1;//设置最大的限度
    private double num_up_acc = up_acc / 2, num_down_acc = down_acc/ 2, num_speed = speed / 2;//初始的数值

    //控制台
    private Button btn_stop;
    private ImageButton ibtn_up, ibtn_down, ibtn_left, ibtn_right;
    private Boolean longPress;//表征是否在按压

    //按钮
    private Button btn_offOnSystem, btn_stopEmergency, btn_detail;
    private boolean isSystemOn = false;

    //误差数据


    private Boolean myBound = false;
    private MyService myService;//service实例, 可以调用service里的公共方法
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_layout);

        Log.e(TAG, "onCreate: " + "two");
        //设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //设置输入法不自动弹出
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initView();

        setSeekBar();



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


//        connection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                MyService.LocalBinder myBinder = (MyService.LocalBinder) iBinder;
//                myService = myBinder.getService();
//                myBound = true;
//                //myService.getContext(mContext);//将上下文传递给service
//                Log.i(TAG, "two: 绑定" );
//            }
//            @Override
//            public void onServiceDisconnected(ComponentName componentName) {
//                myBound = false;
//            }
//        };
//        //开启并绑定服务
//        Intent intent = new Intent(SecondActivity.this, MyService.class);
//        startService(intent);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
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

    //初始化视图
    public void initView(){
        seekBar_up = findViewById(R.id.seekBar_up);
        seekBar_down = findViewById(R.id.seekBar_down);
        seekBar_time = findViewById(R.id.seekBar_time);

        tv_up = findViewById(R.id.tv_up);
        tv_down = findViewById(R.id.tv_down);
        tv_time = findViewById(R.id.tv_time);

        //控制器
        btn_stop = findViewById(R.id.btn_stop);
        ibtn_up = findViewById(R.id.ibtn_up);
        ibtn_down = findViewById(R.id.ibtn_down);
        ibtn_left = findViewById(R.id.ibtn_left);
        ibtn_right = findViewById(R.id.ibtn_right);

        //按钮点击
        btn_offOnSystem = findViewById(R.id.btn_off_on_system);
        btn_stopEmergency = findViewById(R.id.btn_stop_emergency);
        btn_detail = findViewById(R.id.btn_detail);
    }

    //拖动
    public void setSeekBar(){
        seekBar_up.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                num_up_acc = i / 10.0 * up_acc;
                tv_up.setText(num_up_acc + " m/s²");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_down.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                num_down_acc = i / 10.0 * down_acc;
                tv_down.setText(num_down_acc + " m/s²");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                num_speed = i / 10 * speed;
                tv_time.setText(num_speed + " s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //控制台的长按和松开事件
    public void controlButton(){
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmd = StOP + " " + down_acc + ENTER;
                // 发送停车指令
                
            }
        });

        ibtn_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                longPress = false;
                String cmdPress = UP + " " + num_up_acc + " " + num_speed + ENTER;
                String cmdStopPress =  StOP + " " + num_down_acc + ENTER;
                longTouchSendCmd(SecondActivity.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        ibtn_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                longPress = false;
                String cmdPress = DOWN + " " + num_up_acc + " " + num_speed + ENTER;
                String cmdStopPress =  StOP + " " + num_down_acc + ENTER;
                longTouchSendCmd(SecondActivity.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        ibtn_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                longPress = false;
                String cmdPress = LEFT + " " + num_up_acc + " " + num_speed + ENTER;
                String cmdStopPress =  StOP + " " + num_down_acc + ENTER;
                longTouchSendCmd(SecondActivity.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        ibtn_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                longPress = false;
                String cmdPress = RIGHT + " " + num_up_acc + " " + num_speed + ENTER;
                String cmdStopPress =  StOP + " " + num_down_acc + ENTER;
                longTouchSendCmd(SecondActivity.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });
    }
    private void longTouchSendCmd(final Activity activity, String cmdPress, String cmdStopPress, MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{ //持续点击按钮
                longPress = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true){
                            if (longPress){
                                try {
                                    //发送开始指令

                                    Thread.sleep(100);//每0.1s一次
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                break;//没有按下, 退出循环
                            }
                        }
                    }
                }).start();

                break;
            }
            case MotionEvent.ACTION_UP:{
                longPress = false;
                //发送停止指令

            }
        }
    }

    //按钮点击事件
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_off_on_system:
                isSystemOn = !isSystemOn;
                if (isSystemOn){
                    btn_offOnSystem.setText("关闭系统");
                    String cmd = STARTSYSTEM + " " + ENTER;
                    //向所有从平台发送

                    Toast.makeText(this, cmd, Toast.LENGTH_SHORT).show();
                } else{
                    btn_offOnSystem.setText("开启系统");
                    String cmd = STOPSYSTEM + " " + ENTER;
                    //向所有从平台发送

                    Toast.makeText(this, cmd, Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.btn_stop_emergency:
                //紧急停止
                if (isSystemOn){ //平台开着
                    String cmd = StOPEM + " " + ENTER;
                    //发送紧急停止的命令

                    btn_offOnSystem.setText("开启系统");//按紧急停止指令之后, 自动关闭系统
                    isSystemOn = !isSystemOn;
                }
                break;

            case R.id.btn_detail:
                if (!isSystemOn )
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " + "two");
    }
}