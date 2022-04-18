package com.example.mobilelocationapp.fzy;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    private float circleX;
    private float circleY;
    //坐标系位置
    float paintX;
    float paintY;
    //圆半径
    private int radius;
    //声明画笔
    private Canvas my_canvas;
    private Paint paint;
    //屏幕宽高
    int screenWidth;
    int screenHeight;
    //view的宽高
    int ViewWidth;
    int ViewHeight;
    //位图宽高
    int bitmapWidth;
    int bitmapHeight;
    LinearLayout linearLayout;
    //位图左上角坐标
    int bitmapX;
    int bitmapY;
    float textWidth = 3f;
    float textSize = 30;
    MyDrawView myDrawView;
    volatile MyService.MyBinder myBinder;
    MyService myService;
    Boolean myBound = false;
    Button btn_send, btn_create, nextPage;
    TextView txtIP;
    EditText edtShow;
    StringBuffer stringBuffer = new StringBuffer();
    MyBroadcast myBroadcast = new MyBroadcast();
    List<Car> cars = new ArrayList<>();
    AppCompatSeekBar seekBar;
    TextView speedTxt;
    LinkedList<String> formCars = new LinkedList<>();
    TextView formCar;
    Map<Integer, String> carMap = new HashMap<>();
    String car1 = "  从车一";
    String car2 = "  从车二";
    String car3 = "  从车三";
    String UP = "UP 0.1 0.5\r\n";
    String DOWN = "DOWN 0.1 1\r\n";
    String LEFT = "LEFT 0.1 0.1\r\n";
    String RIGHT = "RIGHT 1 0.5\r\n";
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_layout);

        init();

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

        Log.e(TAG, "onCreate: 主页面" );
        IntentFilter intentFilter1 = new IntentFilter("get1102");
        IntentFilter intentFilter2 = new IntentFilter("get1103");
        IntentFilter intentFilter3 = new IntentFilter("get1104");
        registerReceiver(myBroadcast, intentFilter1);
        registerReceiver(myBroadcast, intentFilter2);
        registerReceiver(myBroadcast, intentFilter3);

        //绑定服务
        Intent intent = new Intent(MainActivity2.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "BindService: " + myBinder );

        //启动1s后建立tcp连接
        Handler handler = new Handler();
        Runnable runnable1 = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 在此处添加执行的代码
                if (myBinder != null) {
                    myBinder.createTcpBind();

                }
//                Log.e(TAG, "run: handler;");
                handler.removeCallbacks(this);
            }
        };
        handler.postDelayed(runnable1, 400);// 打开定时器，50ms后执行runnable操作

        //创建定时器检测连接状态
        detectConnect();

        //控制速度进度条
        controlSpeed();
    }

    private void init() {
        linearLayout = findViewById(R.id.linearlayout);
        //创建DrawView组件
        myDrawView = new MyDrawView(this, null);
        //获取布局文件里的linearlayout容器
        linearLayout.addView(myDrawView);

        nextPage = findViewById(R.id.StartFormation);
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
    }

    //检测连接状态
    private void detectConnect() {
        Handler handler = new Handler();
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

                    //在离线情况下将图上离线的点设为黑色
                    for (Car car : cars) {
                        if (status[car.getPort() - 1101].equals("离线")) {
                            erasePoint(car.getPort());
                        }
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
        handler.postDelayed(runnable, 1000);// 打开定时器，50ms后执行runnable操作
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

    private ServiceConnection connection = new ServiceConnection() {
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

                }
                break;
            case R.id.radio1:
                if (checked) {
                    currRadio = 1;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车一");
                }
                    break;
            case R.id.radio2:
                if (checked) {
                    currRadio = 2;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车二");
                }
                    break;
            case R.id.radio3:
                if (checked) {
                    currRadio = 3;
                    Log.e(TAG, "onRadioButtonClicked: " + currRadio );
                    current.setText("从车三");
                }
                    break;
        }
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            if (mAction.equals("get1102") || mAction.equals("get1103") || mAction.equals("get1104")) {
                String message = intent.getStringExtra("V_actual");
                int port = intent.getIntExtra("port", -1);

                if (car_isChecked[port - 1102] == 1) {
                    drawPoint(port, message);
                }

            }
        }
    }

    //消除点
    private void erasePoint(int port) {
        //如果相同端口有点则用白点再画一遍

        for (Car car : cars) {
            if (car.getPort() == port) {
                cars.remove(car);
                myDrawView.erasePoint(car.getName(), car.getX(), car.getY());
                break;
            }
        }
    }

    //画点
    private void drawPoint(int port, String message) {
//        Log.e(TAG, "onReceive: " + port + " " + System.currentTimeMillis());
        String[] messageData = message.split(" ");
        Car update_car = new Car();

        erasePoint(port);

        paintX = Float.parseFloat(messageData[1]) * 100 + circleX;
        paintY = Float.parseFloat(messageData[2]) * 100 + circleY;
//        Log.e(TAG, "drawPoint: " + paintX + ", " + paintY );
        //存储数据，每次画新点之前，将旧点抹去
        update_car.setX(paintX);
        update_car.setY(paintY);
        update_car.setPort(port);
        if (port == 1102) {
            update_car.setName("一");
        } else if (port == 1103) {
            update_car.setName("二");
        } else if (port == 1104) {
            update_car.setName("三");
        }
        cars.add(update_car);

        myDrawView.drawPoint(update_car.getName());
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

            //获取屏幕长宽
            DisplayMetrics metrics = new DisplayMetrics();   //for all android versions
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;

            //设置位图宽高
            bitmapWidth = screenWidth;
            bitmapHeight = screenHeight;

            //设置位图的宽高
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
            //设置位图颜色
            bitmap.eraseColor(Color.WHITE);
            my_canvas = new Canvas(bitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //位图的左上角坐标
//            bitmapX = screenWidth - linearLayout.getLeft() - ViewWidth;
            bitmapX = 0;
            bitmapY = 0;
            //圆心的坐标
            circleX = (float) ViewWidth / 2;
//            circleY = (float) ViewHeight / 2;
            circleY = 0;
            //圆半径
            radius = 550;

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
            paint2.setTextSize(30);
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

        public void drawPoint(String name) {
            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            my_canvas.drawPoint(paintX, paintY, paint);
            my_canvas.drawText(name, paintX + 10, paintY + 10, paint);
            invalidate();
        }

        public void erasePoint(String name, float paintX, float paintY) {
            paint.setStrokeWidth(11f);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            my_canvas.drawPoint(paintX, paintY, paint);
            my_canvas.drawText(name, paintX + 10, paintY + 10, paint);
            invalidate();
        }
    }
}
