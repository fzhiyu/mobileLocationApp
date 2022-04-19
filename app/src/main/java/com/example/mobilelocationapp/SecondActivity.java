package com.example.mobilelocationapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilelocationapp.chart.CarList;
import com.example.mobilelocationapp.chart.MyLineChart;
import com.example.mobilelocationapp.chart.RealPoint;
import com.example.mobilelocationapp.chart.TargetPoint;
import com.example.mobilelocationapp.fzy.MyService;
import com.example.mobilelocationapp.utils.Tools;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

    public static int base_port = 1101; // 主车开始的端口号

    public static final String CAR_Ports = "carPorts";//传过来代表有那些小车进入了编队
    private int[] car_ports;

    public static final String CAR_LISTS = "carLists";
    private int controlledCarNum;
    private HashMap<Integer, CarList> carListsHash = new HashMap<>();//误差数据
    private List<Double> xList, yList;

    //广播的action
    public static final String GET_1102 = "get1102";
    public static final String GET_1103 = "get1103";
    public static final String GET_1104 = "get1104";

    private Context mContext;

    private RelativeLayout rLayout;
    private CarErrorView carErrorView;
    private float m_to_dp = 50;//表示用多少dp代表一米

    private SeekBar seekBar_up, seekBar_down, seekBar_time;//拖动条
    private TextView tv_up, tv_down, tv_time;
    private double up_acc = 1, down_acc = 1, speed = 1;//设置最大的限度
    private double num_up_acc = up_acc / 2, num_down_acc = down_acc/ 2, num_speed = speed / 2;//初始的数值

    //控制台
    private Button btn_stop;
    private ImageButton ibtn_up, ibtn_down, ibtn_left, ibtn_right;
    private Boolean longPress;//表征是否在按压

    //按钮
    private Button btn_offOnSystem, btn_stopEmergency, btn_detail, btn_save;
    private boolean isSystemOn = false;

    //表格
    private LineChart chart_x, chart_y;
    private MyLineChart myLineChart_x, myLineChart_y;

    private Boolean myBound = false;
    private MyService.MyBinder myBinder;
    private MyService myService;//service实例, 可以调用service里的公共方法
    private ServiceConnection connection;

    //广播
    private MyBroadcast myBroadcast = new MyBroadcast();

    //测试
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_layout);

        Log.e(TAG, "onCreate: " + "two");
        //设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //设置输入法不自动弹出
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //得到上个界面传过来的编队的小车, 并创建相应的储存数据的carList
        getCarPorts();
        //测试
        test();

        mContext = this;

        //初始化视图
        initView();

        //设置拖动条
        setSeekBar();

        //表格
        setLineChart();

        //小车
        setCar();

        //设置广播
        setBoardCast();

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myBinder = (MyService.MyBinder) iBinder;
                myService = myBinder.getService();
                myBound = true;
                //myService.getContext(mContext);//将上下文传递给service
                Log.i(TAG, "two: 绑定" );
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                myBound = false;
            }
        };
        //开启并绑定服务
        Intent intent = new Intent(SecondActivity.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void getCarPorts(){
        Intent intent = getIntent();
        car_ports = intent.getIntArrayExtra(CAR_Ports);

        if (car_ports != null){
            controlledCarNum = car_ports.length;
            for (int i = 0; i < controlledCarNum; i++) {
                carListsHash.put(car_ports[i], new CarList(car_ports[i]));
            }
        }

    }

    public void test(){
        car_ports = new int[]{1102, 1103, 1104};

        if (car_ports != null){
            controlledCarNum = car_ports.length;
            for (int i = 0; i < controlledCarNum; i++) {
                carListsHash.put(car_ports[i], new CarList(car_ports[i]));
            }
        }
    }

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
        btn_save = findViewById(R.id.btn_save);

        //小车
        rLayout = findViewById(R.id.rLayout);

        //布局
//        lLayout_X_Error = findViewById(R.id.llayout_X_error);
//        lLayout_Y_Error = findViewById(R.id.llayout_Y_error);
        chart_x = findViewById(R.id.chart_x);
        chart_y = findViewById(R.id.chart_y);
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
                num_speed = i / 10.0 * speed;
                tv_time.setText(num_speed + " m/s");
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
                myBinder.sendMessageBind(cmd, 0, mContext);
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
                                    myBinder.sendMessageBind(cmdPress, 0, mContext);
                                    Thread.sleep(500);//每0.5s一次
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
                myBinder.sendMessageBind(cmdStopPress, 0, mContext);
            }
        }
    }

    //初始化小车
    public void setCar(){
        RealPoint[] cars = new RealPoint[controlledCarNum];
        for (int i = 0; i < controlledCarNum; i++) {
            cars[i] = new RealPoint(car_ports[i], (i + 1) % 2 * Math.pow(-1, i / 2) * 4, (i % 2) * (4));
        }
//        cars[0] = new RealPoint(base_port + 1, 8, 0);
//        cars[1] = new RealPoint(base_port + 2, 0, -4);
//        cars[2] = new RealPoint(base_port + 3, -4, 0);
        carErrorView = new CarErrorView(this, null, cars);
        rLayout.addView(carErrorView);
    }

    public void setBoardCast(){
        IntentFilter filter = new IntentFilter();//过滤器

        //添加代表小车的动作
        filter.addAction(GET_1102);
        filter.addAction(GET_1103);
        filter.addAction(GET_1104);

        //注册广播
        registerReceiver(myBroadcast, filter);
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            if (mAction.equals("get1102") || mAction.equals("get1103") || mAction.equals("get1104")) {
                String v_message = intent.getStringExtra("V_actual");
                int port = intent.getIntExtra("port", -1);
                String target_message = intent.getStringExtra("target");

                //收到v类型的数据后, 更新小车位置, 将小车的真实位置存入carList
                if (v_message != null && !v_message.equals("") && port != -1){

                    String[] messageData = v_message.split(" ");

                    double x = Double.parseDouble(messageData[1]);
                    double y = Double.parseDouble(messageData[2]);

                    RealPoint realPoint = new RealPoint(port, x, y);

                    carErrorView.update(realPoint);//更新小车位置

                    if (isSystemOn){ //如果追踪系统开着, 将数据放入carList中
                        carListsHash.get(port).addRealPoint(realPoint);
                    }
                }

                //收到target类型的数据后, 将小车的理论位置存入carList
                if (target_message != null && !target_message.equals("") && port != -1){
                    if (isSystemOn){
                        String[] messageData = target_message.split(" ");
                        double x = Double.parseDouble(messageData[1]);
                        double y = Double.parseDouble(messageData[2]);

                        TargetPoint targetPoint = new TargetPoint(x, y);

                        if (isSystemOn){
                            carListsHash.get(port).addTargetPoint(targetPoint);
                        }
                    }
                }

            }

        }
    }

    public void setLineChart(){

        myLineChart_x = new MyLineChart(chart_x, "x轴误差");
        myLineChart_x.initLineChart();


        myLineChart_y = new MyLineChart(chart_y, "y轴误差");
        myLineChart_y.initLineChart();

    }

    public void showLine(){
        Set<Integer> set = carListsHash.keySet();
        for (Integer port: set) {
            List<Entry> entryList_slave_x = new ArrayList<>();
            List<Entry> entryList_slave_y = new ArrayList<>();

            int color = Color.BLUE;
            String carName = "从车一";
            switch (port.intValue()){
                case 1102:
                    color = Color.BLUE;
                    carName = "从车一";
                    break;
                case 1103:
                    color = Color.RED;
                    carName = "从车二";
                    break;
                case 1104:
                    color = Color.GRAY;
                    carName = "从车三";
                    break;
            }

            int count_x = 0;
            for (Double x_error: carListsHash.get(port).getXError()) {
                count_x++;
                Entry entry = new Entry(count_x, x_error.floatValue());
                entryList_slave_x.add(entry);
            }

            int count_y = 0;
            for (Double y_error: carListsHash.get(port).getXError()) {
                count_y++;
                Entry entry = new Entry(count_y, y_error.floatValue());
                entryList_slave_y.add(entry);
            }

            myLineChart_x.createLine(entryList_slave_x, color, LineDataSet.Mode.CUBIC_BEZIER, carName);
            myLineChart_y.createLine(entryList_slave_y, color, LineDataSet.Mode.CUBIC_BEZIER, carName);
        }
    }

    //按钮点击事件
    public void onClick(View view) {
        //用完删除
        Timer timer = new Timer();

        switch (view.getId()){
            case R.id.btn_off_on_system:
                isSystemOn = !isSystemOn;
                if (isSystemOn){
                    btn_offOnSystem.setText("关闭系统");
                    String cmd = STARTSYSTEM + " " + ENTER;

                    //向所有从平台发送
                    for (int i = 0; i < controlledCarNum; i++){
                        myBinder.sendMessageBind(cmd, car_ports[i], mContext);
                    }

                    //清除上一次的数据
                    Set<Integer> set = carListsHash.keySet();
                    for (Integer port: set) {
                        carListsHash.get(port).clear();
                    }

                    btn_detail.setVisibility(Button.INVISIBLE);
                    btn_save.setVisibility(Button.INVISIBLE);

                    //用完删除
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            double x = Math.random() * 10 - 5;
                            double y = Math.random() * 5 + 0.1;
                            while (x == 0){
                                x = Math.random() * 10 - 5;
                            }
                            Random random = new Random(1000001010 + System.currentTimeMillis());
                            double target_x = random.nextDouble() * 10 - 5;
                            while (target_x == 0){
                                target_x = Math.random() * 10 - 5;
                            }
                            double target_y = random.nextDouble() * 5 + 0.1;
                            int port = (int)(Math.random() * 3) + 1 + base_port;
                            RealPoint realPoint = new RealPoint(port, x , y);
                            TargetPoint targetPoint = new TargetPoint(target_x, target_y);

                            carListsHash.get(port).addRealPoint(realPoint);
                            carListsHash.get(port).addTargetPoint(targetPoint);

                            carErrorView.update(realPoint);
                        }
                    }, 10, 500);
                } else{
                    btn_offOnSystem.setText("开启系统");
                    String cmd = STOPSYSTEM + " " + ENTER;
                    //向所有从平台发送
                    for (int i = 0; i < controlledCarNum; i++){
                        myBinder.sendMessageBind(cmd, car_ports[i], mContext);
                    }

                    btn_detail.setVisibility(Button.VISIBLE);
                    btn_save.setVisibility(Button.VISIBLE);

                    //用完删除
                    timer.cancel();
                    Set<Integer> set = carListsHash.keySet();
                    for (int i = 0; i < 1000; i++) {
                        for (Integer port: set) {
                            double x = Math.random() * 10 - 5;
                            double y = Math.random() * 5 + 0.01;
                            while (x == 0){
                                x = Math.random() * 10 - 5;
                            }
                            Random random = new Random(i * 12345);
                            double target_x = random.nextDouble() * 10 - 5;
                            double target_y = random.nextDouble() * 5 + 0.01;
                            while (target_x == 0){
                                target_x = Math.random() * 10 - 5;
                            }
                            RealPoint realPoint = new RealPoint(port, x , y);
                            TargetPoint targetPoint = new TargetPoint(target_x, target_y);

                            carListsHash.get(port).addRealPoint(realPoint);
                            carListsHash.get(port).addTargetPoint(targetPoint);
                        }

                    }

                }
                break;
            case R.id.btn_stop_emergency:
                //紧急停止
                if (isSystemOn){ //平台开着
                    String cmd = StOPEM + " " + ENTER;
                    //发送紧急停止的命令
                    for (int i = 0; i < controlledCarNum; i++){
                        myBinder.sendMessageBind(cmd, i, mContext);
                    }

                    btn_offOnSystem.setText("开启系统");//按紧急停止指令之后, 自动关闭系统
                    isSystemOn = !isSystemOn;
                }
                break;

            case R.id.btn_detail:

                showLine();

                break;

            case R.id.btn_save:

                //设置文件夹
                String dirPath = setDir(SecondActivity.this);
                //保存数据到本地
                boolean b = saveFile(dirPath);

                if (b){
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public String setDir(Activity activity){
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//有sd卡
            path = Environment.getExternalStorageDirectory() + "/";
        }else {
            path = Environment.getDataDirectory() + "/";
        }

        path = path + "com.carData/";

        Tools.verifyStoragePermissions(SecondActivity.this);//请求写入的权限

        createDir(path);//创建文件夹

        return path;//返回绝对路径, /storage/emulated/0/com.carData
    }

    public boolean createDir(String path){
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()){
            boolean isCreate = dir.mkdir();
            return  isCreate;
        }

        return true;
    }

    public boolean saveFile(String path){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        Date date = new Date();
        String fileName = dateFormat.format(date) + ".dat";

        File file = new File(path, fileName);

        try (
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file), "UTF-8"));
        ) {
            Set<Integer> set = carListsHash.keySet();
            for (Integer port: set) {
                out.write(port + "\r\n");
                out.write("RealDate: ");

                for (Iterator<RealPoint> iterator = carListsHash.get(port).getRealPointList().iterator(); iterator.hasNext(); ) {
                    String s = iterator.next().toString();
                    out.write(s);
                    out.write(" ");
                }

                out.write("\r\n");
                out.write("Target: ");
                for (Iterator<TargetPoint> iterator = carListsHash.get(port).getTargetPointList().iterator(); iterator.hasNext(); ) {
                    String s = iterator.next().toString();
                    out.write(s);
                    out.write(" ");
                }

                out.write("\r\n");
                out.write("XError: ");
                for (Double d: carListsHash.get(port).getXError()) {
                    out.write(d + " ");
                }

                out.write("\r\n");
                out.write("YError: ");
                for (Double d: carListsHash.get(port).getYError()) {
                    out.write(d + " ");
                }

                out.write("\r\n");
                out.write("\r\n");
            }

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " + "two");

        //注销广播
        unregisterReceiver(myBroadcast);

        //解除绑定
        unbindService(connection);

        //回收表格
        chart_x.clearAllViewportJobs();
        chart_x.removeAllViewsInLayout();
        chart_x.removeAllViews();

        chart_y.clearAllViewportJobs();
        chart_y.removeAllViewsInLayout();
        chart_y.removeAllViews();
    }
}