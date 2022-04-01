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
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilelocationapp.R;
import com.example.mobilelocationapp.SecondActivity;
import com.example.mobilelocationapp.utils.CommendFun;

public class MainActivity2 extends AppCompatActivity {
    float circleX;
    float circleY;
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
    MyService.MyBinder myBinder;
    MyService myService;
    Boolean myBound = false;
    Button btn_send, btn_create, nextPage;
    TextView txtIP;
    EditText edtShow;
    StringBuffer stringBuffer = new StringBuffer();
    MyBroadcast myBroadcast = new MyBroadcast();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        IntentFilter intentFilter = new IntentFilter(TCPServer.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);

        //绑定服务
        Intent intent = new Intent(MainActivity2.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "BindService: " + myBinder );

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
            assert mAction != null;
            if (TCPServer.RECEIVE_ACTION.equals(mAction)) {
                String msg = intent.getStringExtra(TCPServer.RECEIVE_STRING);
                byte[] bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES);
                stringBuffer.append("收到： ").append(msg).append("\n");
                edtShow.setText(stringBuffer);
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
    }
}
