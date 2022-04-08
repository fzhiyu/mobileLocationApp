package com.example.mobilelocationapp.fzy;

import static android.content.ContentValues.TAG;

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
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilelocationapp.R;
import com.example.mobilelocationapp.SecondActivity;
import com.example.mobilelocationapp.utils.CommendFun;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    float circleX;
    float circleY;
    //坐标系位置
    float paintX;
    float paintY;
    //圆半径
    int radius;
    //声明画笔
    private Canvas my_canvas;
    Paint paint;
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
    float textSize = 40;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_main_layout);
        //创建DrawView组件
        myDrawView = new MyDrawView(this, null);
        //获取布局文件里的linearlayout容器
        linearLayout = findViewById(R.id.linearlayout);
        linearLayout.addView(myDrawView);

        btn_send = findViewById(R.id.send);
        btn_create = findViewById(R.id.btn_create);
        nextPage = findViewById(R.id.nextPage);
        txtIP = findViewById(R.id.ip2);
        edtShow = findViewById(R.id.edt_ReceiveMessage);

        //显示IP地址
        txtIP.setText(CommendFun.getLocalIP(getApplicationContext()));
        //控制平板横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //获取view的长宽
        linearLayout = findViewById(R.id.linearlayout);
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
        registerReceiver(myBroadcast, intentFilter1);
        registerReceiver(myBroadcast, intentFilter2);

        //绑定服务
        Intent intent = new Intent(MainActivity2.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "BindService: " + myBinder );


        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 在此处添加执行的代码
                if (myBinder != null) {
                    myBinder.createTcpBind();

                }
                Log.e(TAG, "run: handler;");
                handler.removeCallbacks(this);
            }
        };
        handler.postDelayed(runnable, 1000);// 打开定时器，50ms后执行runnable操作

        Button btn_test = findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                my_canvas.drawPoint();
                myDrawView.drawPoint();
            }
        });

        //建立连接
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myBinder != null) {
                    myBinder.createTcpBind();
                }
            }
        });

        //发送数据
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "test";
                if (myService.tcpServer != null) {
                    myBinder.sendMessageBind(text);
                }

            }
        });

        //跳转下一页
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, SecondActivity.class);
                startActivity(intent);
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

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();

            if (mAction.equals("get1102") || mAction.equals("get1103")) {
                String message = intent.getStringExtra("V_actual");
                int port = intent.getIntExtra("port", -1);

                Log.e(TAG, "onReceive: " + port + " " + System.currentTimeMillis());
                String[] messageData = message.split(" ");
                //如果相同端口有点则用白点再画一遍
                Car update_car = new Car();
                for (Car car : cars) {
                    if (car.getPort() == port){
                        update_car = car;
                        cars.remove(car);
                        myDrawView.erasePoint(update_car.getX(), update_car.getY());
                        break;
                    }
                }

                paintX = Float.parseFloat(messageData[1]) * 100 + circleX;
                paintY = Float.parseFloat(messageData[2]) * 100 + circleY;
                //存储数据，每次画新点之前，将旧点抹去
                update_car.setX(paintX);
                update_car.setY(paintY);
                update_car.setPort(port);
                cars.add(update_car);

                myDrawView.drawPoint();
            }
        }
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
            bitmapX = screenWidth - linearLayout.getLeft() - ViewWidth;
            bitmapY = 0;
            //圆心的坐标
            circleX = bitmapX + (float) ViewWidth / 2;
            circleY = (float) ViewHeight / 2;
            //圆半径
            radius = 450;

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
            canvas.drawCircle(circleX, circleY, 150, paint);
            canvas.drawCircle(circleX, circleY, 250, paint);
            canvas.drawCircle(circleX, circleY, 350, paint);
            canvas.drawCircle(circleX, circleY, 450, paint);
            //画直线
            canvas.drawLine(lineStartX, circleY, lineEndX, circleY, paint);
            float lineStartY = circleY - radius;
            float lineEndY = circleY + radius;
            canvas.drawLine(circleX, lineStartY, circleX, lineEndY, paint);
            //画比例尺
            paint.setTextSize(textSize);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("1:1米", ViewWidth - 300, 100, paint);
            //画一
            canvas.drawText("主", circleX + 10, circleY + 10, paint);

            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            invalidate();
            return true;
        }

        public void drawPoint() {
            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            my_canvas.drawPoint(paintX, paintY, paint);
            invalidate();
        }

        public void erasePoint(float paintX, float paintY) {
            paint.setStrokeWidth(11f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            my_canvas.drawPoint(paintX, paintY, paint);
            invalidate();
        }
    }
}
