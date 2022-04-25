package com.example.mobilelocationapp.fzy;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.example.mobilelocationapp.R;
import com.example.mobilelocationapp.SecondActivity;
import com.example.mobilelocationapp.utils.CommendFun;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    public static final String suffix = "\r\n";
    private volatile Boolean longPress;//表征是否在按压
    private final double num_up_acc = 0;
    private final double num_down_acc = 0;//设置最大的限度
    private float circleX;
    private float circleY;
    //坐标系位置
    private float paintX;
    private float paintY;
    //声明画笔
    private Canvas my_canvas1;
    private Paint paint;
    //view的宽高
    private int ViewWidth;
    private int ViewHeight;
    LinearLayout linearLayout;
    private final float textWidth = 3f;
    private MyDrawView myDrawView;
    private volatile MyService.MyBinder myBinder;
    private MyService myService;
    private Boolean myBound = false;
    private Button btn_send;
    private Button btn_create;
    private TextView txtIP;
    private EditText edtShow;
    private final StringBuffer stringBuffer = new StringBuffer();
    private final MyBroadcast myBroadcast = new MyBroadcast();
    private List<Car> cars = new ArrayList<>();
    private AppCompatSeekBar seekBar;
    private TextView speedTxt;
    private final LinkedList<String> formCars = new LinkedList<>();
    private TextView formCar;
    String car1 = "  从车一";
    String car2 = "  从车二";
    String car3 = "  从车三";
    String UP = "UP 0 ";
    String DOWN = "DOWN 0 ";
    String LEFT = "LEFT 0 ";
    String RIGHT = "RIGHT 0 ";
    String STOP = "STOP 1\r\n";
    String ROTATEL  = "ROTATEL 1\r\n";
    String ROTATER  = "ROTATER 1\r\n";
    String TLEFT  = "TLEFT 1\r\n";
    String TRIGHT  = "TRIGHT 1\r\n";
    String StartFineTurn = "StartFineTurn\r\n";
    String StopFineTurn = "StopFineTurn\r\n";
    String StartFormation = "StartFormation\r\n";
    String StopFormation = "StopFormation\r\n";
    //当前选择的radio
    int currRadio = 0;
    RadioButton radio1;
    RadioButton radio2;
    RadioButton radio3;
    TextView txtCar1;
    TextView txtCar2;
    TextView txtCar3;
    TextView txtCar4;
    TextView current;
    Button btnFineTurn;
    ImageButton TLeft;
    ImageButton TRight;
    String[] status = new String[4];
    //标志位
    int[] car_isChecked = new int[3];
    float speed = 0.30f;
    Paint paint2;
    Map<Integer, String> map = new HashMap<>();
    Map<Integer, Car> carMap = new HashMap<>();
    DecimalFormat df = new DecimalFormat("#.##");
    private ImageButton btn_up, btn_down, btn_left, btn_right, RotateLeft, RotateRight;
    Handler handler = new Handler();
    Runnable runnable1, runnable2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_layout);

        init();

        //创建服务，连接广播
        connect();

        //创建定时器检测连接状态
        detectConnect();

        //控制速度进度条
        controlSpeed();

        //新建线程，画点，作为一个集合，一秒画一次
        drawAll();

        //设置按钮响应
        setButton();
    }

    //绑定服务，创建广播
    private void connect() {
        Log.e(TAG, "onCreate: 主页面" );
        IntentFilter intentFilter1 = new IntentFilter("get1102");
        IntentFilter intentFilter2 = new IntentFilter("get1103");
        IntentFilter intentFilter3 = new IntentFilter("get1104");
        registerReceiver(myBroadcast, intentFilter1);
        registerReceiver(myBroadcast, intentFilter2);
        registerReceiver(myBroadcast, intentFilter3);

        //如果已经绑定，就不再绑定服务
        if (myBinder == null) {
            Intent intent = new Intent(MainActivity2.this, MyService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "BindService: " + myBinder);
        }

        //启动1s后建立tcp连接
        //如果是已经建立过连接，就不再建立连接

        Handler handler = new Handler();
        Runnable runnable1 = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 在服务已经创建好的情况下，并且不重复创建服务器
                if (myBinder != null && myService.isCreate == 0) {
                    myBinder.createTcpBind();
                }
//                Log.e(TAG, "run: handler;");
                handler.removeCallbacks(this);
            }
        };
        //1秒后执行操作
        handler.postDelayed(runnable1, 1000);// 打开定时器，50ms后执行runnable操作
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButton() {

        btn_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = UP + speed + suffix;
                String cmdStopPress =  STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
//                Log.e(TAG, "onTouch: " + motionEvent );
                return true;
            }
        });
        btn_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = DOWN + speed + suffix;
                String cmdStopPress =  STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        btn_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = LEFT + speed + suffix;
                String cmdStopPress =  STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        btn_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = RIGHT + speed + suffix;
                String cmdStopPress =  STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        RotateLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = ROTATEL + suffix;
                String cmdStopPress = STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });

        RotateRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                longPress = false;
                String cmdPress = ROTATER + suffix;
                String cmdStopPress = STOP;
                longTouchSendCmd(MainActivity2.this, cmdPress, cmdStopPress, motionEvent);
                return true;
            }
        });
    }

    private void longTouchSendCmd(Activity activity, String cmdPress, String cmdStopPress, MotionEvent event){

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{ //持续点击按钮
                longPress = true;

//                Log.e(TAG, "longTouchSendCmd: 按下" );
                runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        if (longPress) {
                            myBinder.sendMessageBind(cmdPress, currRadio, getApplicationContext());
//                            Log.e(TAG, "run: " + System.currentTimeMillis() + " " + new Date());
                        }
                        handler.postDelayed(this, 1000);
                    }
                };

                handler.postDelayed(runnable1, 1);
                break;
            }
            case MotionEvent.ACTION_UP:{
                longPress = false;
                handler.removeCallbacks(runnable1);
//                Log.e(TAG, "longTouchSendCmd: 松开" );
                //发送停止指令
                myBinder.sendMessageBind(cmdStopPress, currRadio, getApplicationContext());
            }
        }
    }

    private void init() {
        linearLayout = findViewById(R.id.linearlayout);
        //创建DrawView组件
        myDrawView = new MyDrawView(this, null);
        //获取布局文件里的linearlayout容器
        linearLayout.addView(myDrawView);

        Button nextPage = findViewById(R.id.StartFormation);
        txtIP = findViewById(R.id.ip2);
        seekBar = findViewById(R.id.seekBar_speed);
        speedTxt = findViewById(R.id.speedTxt2);
//        formCar = findViewById(R.id.formCar);
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);
        radio3 = findViewById(R.id.radio3);
        txtCar1 = findViewById(R.id.txt_car1);
        txtCar2 = findViewById(R.id.txt_car2);
        txtCar3 = findViewById(R.id.txt_car3);
        txtCar4 = findViewById(R.id.txt_car4);
        current = findViewById(R.id.current2);
        btnFineTurn = findViewById(R.id.fineTurn);
        TLeft = findViewById(R.id.TLeft);
        TRight = findViewById(R.id.TRight);
        btn_up = findViewById(R.id.ibtn_up);
        btn_down = findViewById(R.id.ibtn_down);
        btn_left = findViewById(R.id.ibtn_left);
        btn_right = findViewById(R.id.ibtn_right);
        RotateLeft = findViewById(R.id.RotateLeft);
        RotateRight = findViewById(R.id.RotateRight);

        //显示IP地址
        txtIP.setText(CommendFun.getLocalIP(getApplicationContext()));
        //控制平板横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //设置速度
        speedTxt.setText(speed + "m/s");

        //获取view的长宽
        ViewTreeObserver observer = linearLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewHeight = linearLayout.getHeight();
                ViewWidth = linearLayout.getWidth();
            }
        });
    }

    //检测连接状态
    private void detectConnect() {
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                if (myService != null) {
                    //设置在线和离线状态
                    status = myService.getStatus();
                    String curr1 = String.valueOf(txtCar1.getText());
                    if (!curr1.equals(status[0]) && status[0].equals("在线")) {
                        txtCar1.setText(status[0]);
                        txtCar1.setTextColor(Color.RED);

                    } else if (!curr1.equals(status[0]) && status[0].equals("离线")) {
                        txtCar1.setText(status[0]);
                        txtCar1.setTextColor(Color.BLACK);
                    }
                    if (status[1].equals("在线")) {
                        txtCar2.setText(status[1]);
                        txtCar2.setTextColor(Color.RED);
                    } else if (status[1].equals("离线")) {
                        txtCar2.setText(status[1]);
                        txtCar2.setTextColor(Color.BLACK);
                    }
                    if (status[2].equals("在线")) {
                        txtCar3.setText(status[2]);
                        txtCar3.setTextColor(Color.RED);
                    } else if (status[2].equals("离线")) {
                        txtCar3.setText(status[2]);
                        txtCar3.setTextColor(Color.BLACK);
                    }
                    if (status[3].equals("在线")) {
                        txtCar4.setText(status[3]);
                        txtCar4.setTextColor(Color.RED);
                    } else if (status[3].equals("离线")) {
                        txtCar4.setText(status[3]);
                        txtCar4.setTextColor(Color.BLACK);
                    }

                    //处理连接异常,如果超过2.5秒没有收到心跳信息，就将连接关闭，并改变状态
//                    long[] heartTime = myService.getHeart();
//                    long curr = System.currentTimeMillis();
//                    for (int i = 0; i < 4; i++) {
//                        if (heartTime[i] != 0 && curr - heartTime[i] > 2500) {
//                            switch (i) {
//                                case 0:
//                                    txtCar1.setTextColor(Color.BLACK);
//                                case 1:
//                                    txtCar2.setTextColor(Color.BLACK);
//                                case 2:
//                                    txtCar3.setTextColor(Color.BLACK);
//                                case 3:
//                                    txtCar4.setTextColor(Color.BLACK);
//                            }
//                            //关闭socket
//                            myService.closeSocket(i);
//                        }
//                    }
//                    Log.e(TAG, "run: " + status[3] );
                }
                handler.postDelayed(this, 500);// 50ms后执行this，即runable
            }
        };
        handler.postDelayed(runnable, 1100);// 打开定时器，50ms后执行runnable操作
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " );
    }

    private void controlSpeed() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = (float) i / 100;
                speedTxt.setText(speed + " m/s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();
            myBound = true;
            Log.e(TAG, "onServiceConnected: 绑定" );
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myBound = false;
        }
    };

    //设置复选框
    @SuppressLint("NonConstantResourceId")
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox1:
                if (checked) {
                    car_isChecked[0] = 1;
                    formCars.addFirst(car1);
                    radio1.setEnabled(true);
                } else {
                    car_isChecked[0] = 0;
                    formCars.remove(car1);
                    radio1.setEnabled(false);
                    radio1.setChecked(false);
                    erasePoint(1102);
                }
                convertCar(formCars);
                break;
            case R.id.checkbox2:
                if (checked) {
                    if (formCars.size() == 2 && formCars.getLast().equals(car3)) {
                        formCars.add(1, car2);
                    } else if (formCars.size() == 1 && formCars.get(0).equals(car3)){
                        formCars.addFirst(car2);
                    } else {
                        formCars.add(car2);
                    }
                    radio2.setEnabled(true);
                    car_isChecked[1] = 1;
                } else {
                    radio2.setEnabled(false);
                    radio2.setChecked(false);
                    formCars.remove(car2);
                    car_isChecked[1] = 0;
                    erasePoint(1103);
                }
                convertCar(formCars);
                break;
            case R.id.checkbox3:
                if (checked) {
                    formCars.addLast(car3);
                    radio3.setEnabled(true);
                    car_isChecked[2] = 1;
                } else {
                    formCars.remove(car3);
                    radio3.setEnabled(false);
                    radio3.setChecked(false);
                    car_isChecked[2] = 0;
                    erasePoint(1104);
                }
                convertCar(formCars);
                break;
            // TODO: Veggie sandwich
        }
    }

    private void convertCar(List<String> formCars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : formCars) {
            stringBuilder.append(s);
        }
//        formCar.setText(stringBuilder);
    }

    public void StartFormation(View view) {
        handler.removeCallbacks(runnable2);
        myBinder.sendMessageBind(StartFormation, currRadio, getApplicationContext());
        Intent intent = new Intent(MainActivity2.this, SecondActivity.class);
        startActivity(intent);
    }

    public void sendStop(View view) {
        myBinder.sendMessageBind(STOP, currRadio, getApplicationContext());
    }

    public void sendUp(View view) {
        myBinder.sendMessageBind(UP, currRadio, getApplicationContext());
    }

    public void sendDown(View view) {
        myBinder.sendMessageBind(DOWN, currRadio, getApplicationContext());
    }

    public void sendLeft(View view) {
        myBinder.sendMessageBind(LEFT, currRadio, getApplicationContext());
    }

    public void sendRight(View view) {
        myBinder.sendMessageBind(RIGHT, currRadio, getApplicationContext());
    }

    public void sendRotateLeft(View view) {
        myBinder.sendMessageBind(ROTATEL, currRadio, getApplicationContext());
    }

    public void sendRotateRight(View view) {
        myBinder.sendMessageBind(ROTATER, currRadio, getApplicationContext());
    }

    public void fineTurn(View view) {
        if (btnFineTurn.getText().equals("关闭微调")) {
            TLeft.setEnabled(false);
            TRight.setEnabled(false);
            btnFineTurn.setText("开启微调");
            myBinder.sendMessageBind(StopFineTurn, currRadio, getApplicationContext());
        } else {
            TLeft.setEnabled(true);
            TRight.setEnabled(true);
            btnFineTurn.setText("关闭微调");
            myBinder.sendMessageBind(StartFineTurn, currRadio, getApplicationContext());
        }
    }

    public void sendTRight(View view) {
        myBinder.sendMessageBind(TRIGHT, currRadio, getApplicationContext());
    }

    public void sendTLeft(View view) {
        myBinder.sendMessageBind(TLEFT, currRadio, getApplicationContext());
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio0:
                if (checked) {
                    currRadio = 0;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    btnFineTurn.setEnabled(false);
                }
                break;
            case R.id.radio1:
                if (checked) {
                    currRadio = 1;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车一");
                    btnFineTurn.setEnabled(true);
                }
                    break;
            case R.id.radio2:
                if (checked) {
                    currRadio = 2;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车二");
                    btnFineTurn.setEnabled(true);
                }
                    break;
            case R.id.radio3:
                if (checked) {
                    currRadio = 3;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车三");
                    btnFineTurn.setEnabled(true);
                }
                    break;
        }
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            if (mAction.equals("get1102") || mAction.equals("get1103") || mAction.equals("get1104")) {
                String message = intent.getStringExtra("V_actual");
                int port = intent.getIntExtra("port", -1);

                //可以考虑一次画多个，一次性画点，目前是多次画点，中间易干扰
                //每一秒读取一次数据
//                if (car_isChecked[port - 1102] == 1) {
//                    drawPoint(port, message);
//                }
                //先存数据,在复选框选中的时候，才在图上显示
                if (car_isChecked[port - 1102] == 1) {
                    map.put(port, message);
                }
            }
        }
    }

    private void drawAll() {
        runnable2 = new Runnable() {
            @Override
            public void run() {
                //每一秒读取一次map, 画点，擦除点的时候，直接擦除整体
                for (Map.Entry<Integer, Car> entry : carMap.entrySet()) {
                    //每次画点之前，先整体擦除，再画点
                    erasePoint(entry.getKey());
//                    Log.e(TAG, "run: " + entry );
                }

                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    //每次画点之前，先整体擦除，再画点
                    if (status[entry.getKey() - 1101].equals("在线")) {
                        drawPoint(entry.getKey(), entry.getValue());
                    }
                }

                //消除离线的点

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable2, 1000);
    }

    //消除点
    private void erasePoint(int port) {
        //如果相同端口有点则用白点再画一遍

        for (Map.Entry<Integer, Car> entry : carMap.entrySet()) {
            if (entry.getKey() == port) {
//                cars.remove(car);
                myDrawView.erasePoint(
                        entry.getValue().getName(),
                        entry.getValue().getX(),
                        entry.getValue().getY(),
                        entry.getValue().getLength(),
                        entry.getValue().getRadius());
                break;
            }
        }
    }

    //画点
    private void drawPoint(int port, String message) {
//        Log.e(TAG, "onReceive: " + port + " " + System.currentTimeMillis());
        String[] messageData = message.split(" ");
        //可以只更新点的坐标，不删除点的坐标

//        erasePoint(port);

        paintX = Float.parseFloat(messageData[1]) * 100 + circleX;
        paintY = Float.parseFloat(messageData[2]) * 100 + circleY;
        //以圆心为原点更改坐标系, 更改显示坐标点
        float showX = paintX - circleX;
        float showY = paintY - circleY;
        //将点的长度和角度写入
        float paintToCenter = showX * showX + showY * showY;
        double lengthPoint = Math.sqrt(paintToCenter) / 100;
        double radiusPoint = Math.asin(showY / Math.sqrt(paintToCenter)) * 180 / Math.PI;
//        Log.e(TAG, "drawPoint: " + paintX + ", " + paintY );
        //存储数据，每次画新点之前，将旧点抹去, 改变存储结构
        if (port == 1102) {
            carMap.put(port, new Car(paintX, paintY, lengthPoint, radiusPoint, "一"));
        } else if (port == 1103) {
            carMap.put(port, new Car(paintX, paintY, lengthPoint, radiusPoint, "二"));
        } else if (port == 1104) {
            carMap.put(port, new Car(paintX, paintY, lengthPoint, radiusPoint, "三"));
        }

        myDrawView.drawPoint(
                carMap.get(port).getName(),
                carMap.get(port).getLength(),
                carMap.get(port).getRadius());
    }

    public class MyDrawView extends View {

        private static final String TAG = "Draw";
        Path path;
        //声明位图
        private final Bitmap bitmap;

        public MyDrawView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            //The Paint class holds the style and color information
            //about how to draw geometries, text and bitmaps.
            paint = new Paint();
            path = new Path();
            paint2 = new Paint();

            //获取屏幕长宽
            DisplayMetrics metrics = new DisplayMetrics();   //for all android versions
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            //屏幕宽高
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            //设置位图宽高
            //位图宽高
            int bitmapWidth = screenWidth;
            int bitmapHeight = screenHeight;

            //设置位图的宽高
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
            //设置位图颜色
            bitmap.eraseColor(Color.WHITE);
            my_canvas1 = new Canvas(bitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //位图的左上角坐标
//            bitmapX = screenWidth - linearLayout.getLeft() - ViewWidth;
            //位图左上角坐标
            int bitmapX = 0;
            int bitmapY = 0;
            //圆心的坐标
            circleX = (float) ViewWidth / 2;
//            circleY = (float) ViewHeight / 2;
            circleY = 0;
            //圆半径
            //圆半径
            int radius = 550;

            //设置直线的起始点
            float lineStartX = circleX - radius;
            float lineEndX = circleX + radius;
            //设置解释文字点
            float textProportion;

            paint.setStyle(Paint.Style.STROKE);
            canvas.drawBitmap(bitmap, bitmapX, bitmapY, paint);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2f);
            paint.setAntiAlias(true);

            //设置画笔paint2, 点与原点的连接线
            paint2.setStyle(Paint.Style.FILL);
            paint2.setAntiAlias(true);

            //画圆图
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(circleX, circleY, 10, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(new DashPathEffect(new float[]{2,4},50));
            canvas.drawCircle(circleX, circleY, 50, paint);
            canvas.drawCircle(circleX, circleY, 100, paint);
            canvas.drawCircle(circleX, circleY, 150, paint);
            canvas.drawCircle(circleX, circleY, 200, paint);
            canvas.drawCircle(circleX, circleY, 250, paint);
            canvas.drawCircle(circleX, circleY, 300, paint);
            canvas.drawCircle(circleX, circleY, 350, paint);
            canvas.drawCircle(circleX, circleY, 400, paint);
            canvas.drawCircle(circleX, circleY, 450, paint);
            canvas.drawCircle(circleX, circleY, 500, paint);
            canvas.drawCircle(circleX, circleY, 550, paint);
            Paint paint2 = new Paint();
            paint2.setColor(Color.BLACK);
            paint2.setTextSize(25);
            canvas.drawText("1m", circleX - 30, 95, paint2);
            canvas.drawText("2m", circleX - 30, 195, paint2);
            canvas.drawText("3m", circleX - 30, 295, paint2);
            canvas.drawText("4m", circleX - 30, 395, paint2);
            canvas.drawText("5m", circleX - 30, 495, paint2);
            //画直线
            canvas.drawLine(lineStartX, circleY, lineEndX, circleY, paint);
            float lineStartY = circleY - radius;
            float lineEndY = circleY + radius;
            canvas.drawLine(circleX, lineStartY, circleX, lineEndY, paint);
            //画比例尺
            float textSize = 20;
            paint.setTextSize(textSize);
            paint.setStyle(Paint.Style.FILL);
//            canvas.drawText("1格 : 0.5米", ViewWidth -300, 400, paint);
            //画一
            canvas.drawText("主", circleX + 10, circleY + 30, paint);

            paint.setStyle(Paint.Style.FILL);

            paint.setStrokeWidth(20f);
//            my_canvas.drawPoint(10, 10, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            invalidate();
            return true;
        }

        //画点的同时画边 分别在三个画布上画图，彼此互不干扰
        public void drawPoint(String name, double lengthPoint, double radiusPoint) {
            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            my_canvas1.drawPoint(paintX, paintY, paint);
            my_canvas1.drawText(name, paintX + 10, paintY + 10, paint);
            my_canvas1.drawText(
                    df.format(lengthPoint) + "m"+ "," + df.format(radiusPoint) + "度",
                    (paintX + circleX) / 2,
                    (paintY + circleY) / 2,
                    paint);

            paint2.setStyle(Paint.Style.FILL);
            paint2.setStrokeWidth(2f);
            paint2.setColor(Color.BLACK);
            my_canvas1.drawLine(circleX, circleY, paintX, paintY, paint2);
            invalidate();
        }

        public void drawPoint2(String name, Canvas canvas) {

        }

        public void erasePoint(String name, float paintX, float paintY, double lengthPoint, double radiusPoint) {
            paint.setStrokeWidth(11f);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            my_canvas1.drawPoint(paintX, paintY, paint);
            my_canvas1.drawText(name, paintX + 10, paintY + 10, paint);
            my_canvas1.drawText(
                    df.format(lengthPoint) + "m"+ "," + df.format(radiusPoint) + "度",
                    (paintX + circleX) / 2,
                    (paintY + circleY) / 2,
                    paint);

            paint2.setStrokeWidth(4f);
            paint2.setColor(Color.WHITE);
            paint2.setStyle(Paint.Style.FILL);
            my_canvas1.drawLine(circleX, circleY, paintX, paintY, paint2);
            invalidate();
        }

        public void erasePoint2(String name, float paintX, float paintY, Canvas canvas) {

        }

    }
}
