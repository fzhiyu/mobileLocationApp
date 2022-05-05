package com.example.mobilelocationapp.First;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilelocationapp.R;
import com.example.mobilelocationapp.SecondActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "main";
    MyDrawView myDrawView;
//    EditText edt1;
//    EditText edt2;
    EditText edt3;
    EditText edt4;
    RadioButton radio1, radio2, radio3, radio4;
    RadioGroup radioGroup;
    Button btnChange;
    private Button btnFirstToSecond;//首页到第二页的按钮
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
    //触摸点坐标
    float xPos;
    float yPos;
    float paintX;
    float paintY;
    float circleX;
    float circleY;
    //圆半径
    int radius;
    //显示坐标 以圆心为原点
    float showX;
    float showY;
    //以长度角度为要素显示点坐标
    double lengthPoint;
    double radiusPoint;
    //记录点击次数
    int hits = 0;
    TextView text1;
    List<Car> cars = new ArrayList<>();
    int checkedNum = 0;
    Map<String, Boolean> checkedMap = new HashMap<>();
    RadioButton selectedButton;
    DecimalFormat df = new DecimalFormat("#.##");
    String selectedName;
    boolean checkedFlag = false;
    int selectedId;
    float textWidth = 3f;
    float textSize = 40;
    float pointWidth = 8f;
    TextView txtSecond, txtThird, txtFourth;
    //比例换算参数
    double pro = 4.5;
    //修改的参数
    float changeLength;
    float changeRadius;
    float changeX;
    float changeY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);
        //创建DrawView组件
        myDrawView = new MyDrawView(this, null);
        //获取布局文件里的linearlayout容器
        linearLayout = findViewById(R.id.linearlayout);
        linearLayout.addView(myDrawView);

        //控制平板横向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //获取edittext、button、textview、checkbox对象
//        edt1 = findViewById(R.id.edt1);
//        edt2 = findViewById(R.id.edt2);
        edt3 = findViewById(R.id.edt3);
        edt4 = findViewById(R.id.edt4);
        text1 = findViewById(R.id.textClear);
        radio1 = findViewById(R.id.radio0);
        radio2 = findViewById(R.id.radio1);
        radio3 = findViewById(R.id.radio2);
        radio4 = findViewById(R.id.radio3);
        radioGroup = findViewById(R.id.radioGroup);
        btnChange = findViewById(R.id.btn_change);
        btnFirstToSecond = findViewById(R.id.btn_first_to_second);
        txtSecond = findViewById(R.id.second);
        txtThird = findViewById(R.id.third);
        txtFourth = findViewById(R.id.fourth);

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

        //设置输入法不自动弹出
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Button btn_test = findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(10f);
                paint.setTextSize(textSize);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                my_canvas.drawText("极度", 100, 100, paint);
                Log.e(TAG, "onClick: 打印" );
            }
        });

        //清空记录
        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Car car : cars) {
                    paint.setStrokeWidth(pointWidth);
                    paint.setColor(Color.WHITE);
                    my_canvas.drawPoint(car.getX(), car.getY(), paint);
                    paint.setStrokeWidth(textWidth);
                    paint.setTextSize(textSize);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    my_canvas.drawText(car.getName(), car.getX() + 10,car.getY() + 10, paint);
                }
                txtSecond.setText("");
                txtThird.setText("");
                txtFourth.setText("");
                edt3.setText("");
                edt4.setText("");
                hits = 0;
                cars.clear();
            }
        });

        //修改点的坐标
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedId = radioGroup.getCheckedRadioButtonId();
                selectedButton = findViewById(selectedId);
                //检查是否有选项选择
                if (selectedId != -1) {
                    selectedName = (String) selectedButton.getText();
                    Car car = checkRadio();
                    Log.e(TAG, "onClick: " + cars.toString() );
                    Log.e(TAG, "onClick: " + car );
                    //检查被选择项是否已有坐标
                    if (checkRadio() != null) {
                        //如果有坐标则更改
                        changeAxis(car);
                    }
                }
            }
        });

        btnFirstToSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                //传输数据给第二页
                intent.putExtra("something",1);
                startActivity(intent);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                selectedButton = findViewById(checkedId);
                //检查是否有选项选择
                if (selectedId != -1) {
                    selectedName = (String) selectedButton.getText();
                    Car car = checkRadio();
                    //检查被选择项是否已有坐标
                    if (car != null && checkRadio() != null) {
                        //如果有坐标则更改
                        edt3.setText(df.format(car.getLength()));
                        edt4.setText(df.format(car.getRadius()));
                    }
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void changeAxis(Car car) {
        //检测输入是否合法
        try{
            changeLength = Float.parseFloat(edt3.getText().toString());
            changeRadius = Float.parseFloat(edt4.getText().toString());
            if (changeLength < pro && changeRadius < 360) {
                changeX = (float)(changeLength * Math.cos(Math.toRadians(changeRadius)) * radius / pro + circleX);
                changeY = (float) (changeLength * Math.sin(Math.toRadians(changeRadius)) * radius / pro + circleY);
                changeAxis2(car);
            } else if (changeLength > pro) {
                Toast.makeText(getApplicationContext(), "超过最大输入长度", Toast.LENGTH_SHORT).show();
            } else if (changeRadius > 360) {
                Toast.makeText(getApplicationContext(), "超过最大输入角度", Toast.LENGTH_SHORT).show();
            }
        }catch (NumberFormatException ex) {
            Toast.makeText(getApplicationContext(), "输入内容不合法，请重新输入", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void changeAxis2(Car car) {

        //擦粗原有点
        paint.setStrokeWidth(pointWidth);
        paint.setColor(Color.WHITE);
        my_canvas.drawPoint(car.getX(), car.getY(), paint);
        paint.setStrokeWidth(textWidth);
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        my_canvas.drawText(car.getName(), car.getX() + 10,car.getY() + 10, paint);

        cars.remove(car);
        String radio = car.getName();
        Car car1 = new Car(changeX, changeY, changeLength, changeRadius, radio);
        cars.add(car1);
        switch (radio) {
            case "二":
                txtSecond.setText("长度:" + df.format(changeLength) + ",角度:" + df.format(changeRadius));
                break;
            case "三":
                txtThird.setText("长度:" + df.format(changeLength) + ",角度:" + df.format(changeRadius));
                break;
            case "四":
                txtFourth.setText("长度:" + df.format(changeLength) + ",角度:" + df.format(changeRadius));
                break;
            default:
                break;
        }

        paint.setStrokeWidth(pointWidth);
        paint.setColor(Color.BLACK);
        my_canvas.drawPoint(car1.getX(), car1.getY(), paint);
        paint.setStrokeWidth(textWidth);
        paint.setTextSize(textSize);
        my_canvas.drawText(car1.getName(), car1.getX() + 10,car1.getY() + 10, paint);
    }

    private Car checkRadio() {
        for (Car car : cars) {
            if (car.getName().equals(selectedName)) {
                return car;
            }
        }
        return null;
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
            circleY = (float) ViewHeight/2;
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
            canvas.drawCircle(circleX, circleY, 3, paint);
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

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            xPos = event.getX();
            yPos = event.getY();
            //设置画点的位置坐标
            paintX = xPos - bitmapX;
            paintY = yPos;
            //计算点的长度角度

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    paint.setStrokeWidth(5f);
                    drawPoint();
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(xPos, yPos);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    return false;
            }
            //是绘图效果生效
            invalidate();
            return true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void drawPoint() {

        //计算触摸点到圆心距离
        float paintToCenter = (paintX - circleX) * (paintX - circleX) +
                (paintY - circleY) * (paintY - circleY);
        //以圆心为原点更改坐标系, 更改显示坐标点
        showX = paintX - circleX;
        showY = paintY - circleY;
        //获取点的长度角度显示
        lengthPoint = Math.sqrt(showX * showX + showY * showY) / radius * pro;
//        radiusPoint = Math.asin(showY / Math.sqrt(paintToCenter)) * 180 / Math.PI;
        if (showX < 0) {
            radiusPoint = 180 - Math.asin(showY / Math.sqrt(paintToCenter)) * 180 / Math.PI;
        } else if (showY < 0) {
            radiusPoint = 360 + Math.asin(showY / Math.sqrt(paintToCenter)) * 180 / Math.PI;
        } else {
            radiusPoint = Math.asin(showY / Math.sqrt(paintToCenter)) * 180 / Math.PI;
        }

        //获取选框的字符
        selectedId = radioGroup.getCheckedRadioButtonId();
        selectedButton = findViewById(selectedId);
        if (selectedId != -1) {
            selectedName = (String) selectedButton.getText();
            //检查是否重复选择
            checkedFlag = false;
            for (Car car : cars) {
                if (car.getName().equals(selectedName)) {
                    checkedFlag = true;
                    break;
                }
            }
        }

        //限制在圆内画点
        if (paintToCenter < radius * radius && hits < 4 && !checkedFlag && selectedId != -1) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(pointWidth);
            my_canvas.drawPoint(paintX, paintY, paint);
            paint.setStrokeWidth(textWidth);
            paint.setTextSize(textSize);
            paint.setStyle(Paint.Style.FILL);
            my_canvas.drawText(selectedName, paintX + 10, paintY + 10, paint);
//            edt1.setText(df.format(showX));
//            edt2.setText(df.format(-showY));
            edt3.setText(df.format(lengthPoint));
            edt4.setText(df.format(radiusPoint));
            switch (selectedName) {
                case "二":
                    txtSecond.setText("长度:" + df.format(lengthPoint) + ",角度:" + df.format(radiusPoint));
                    break;
                case "三":
                    txtThird.setText("长度:" + df.format(lengthPoint) + ",角度:" + df.format(radiusPoint));
                    break;
                case "四":
                    txtFourth.setText("长度:" + df.format(lengthPoint) + ",角度:" + df.format(radiusPoint));
                    break;
                default:
                    break;
            }

            //添加新的坐标
            cars.add(new Car(paintX, paintY, lengthPoint, radiusPoint, selectedName));
            System.out.println(cars.toString());
            hits = hits + 1;
        } else if (paintToCenter > radius * radius) {
            Toast.makeText(getApplicationContext(), "请在区域内点击", Toast.LENGTH_SHORT).show();
        } else if (hits >= 4) {
            Toast.makeText(getApplicationContext(), "超过最大点击次数", Toast.LENGTH_SHORT).show();
        } else if (checkedFlag) {
            Toast.makeText(getApplicationContext(), "不能重复选择", Toast.LENGTH_SHORT).show();
        } else if (selectedId == -1) {
            Toast.makeText(getApplicationContext(), "请选择后点击", Toast.LENGTH_SHORT).show();
        }
    }

}