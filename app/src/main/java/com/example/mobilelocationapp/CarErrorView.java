package com.example.mobilelocationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.mobilelocationapp.chart.RealPoint;

public class CarErrorView extends View {
    private static final float STROKE_WIDTH = 1F / 256F; // 描边宽度
    private static final int Base_port = 1101;//主车的端口

    private Paint circlePaint;//画车
    private Paint linePaint;//画线
    private Paint carNumberPaint;//画代表小车编号的文字
    private Paint textPaint;//画文字
    private Context mContext;//上下文引用

    private float m_to_dp = 50;//表示用多少dp代表一米
    private int textSize = 20;//文字大小
    private float strokeWidth; //描边宽度
    private float radius = 20; //小车的半径

    private int size, sizeW, sizeH;//控件边长
    private int ccX, ccY; // 圆心坐标

    private float carNumberOffsetY;//小车编号文本的Y轴偏移量
    private float textOffsetY;//文本的Y轴偏移量

    private RealPoint[] cars; // 代表所有的从车
    private int carsNumber;//小车的数量

    public CarErrorView(Context context) {
        this(context, null);
    }

    public CarErrorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, null);
    }

    public CarErrorView(Context context, @Nullable AttributeSet attrs, RealPoint[] cars){
        super(context, attrs);

        mContext = context;
        this.cars = cars;
        carsNumber = cars.length;

        //初始化画笔
        initPaint();
    }

    private void initPaint(){
        //初始化圆画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//新建画笔并设置画笔抗锯齿
        circlePaint.setStyle(Paint.Style.FILL);//填充
        circlePaint.setColor(Color.BLUE);//设置画笔颜色为蓝色
        circlePaint.setStrokeWidth(strokeWidth);

        //初始化小车编号画笔
        carNumberPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        carNumberPaint.setColor(Color.WHITE);
        carNumberPaint.setTextSize(textSize);
        carNumberPaint.setTextAlign(Paint.Align.CENTER);

        carNumberOffsetY = (carNumberPaint.ascent() + carNumberPaint.descent()) / 2;

        //初始化文字画笔
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textOffsetY = (textPaint.ascent() + textPaint.descent()) / 2;

        //初始化线段画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//新建画笔并设置画笔抗锯齿
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //强制长宽一样
//        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //获取控件的边长
        sizeW = w;
        sizeH = h;
        size = Math.min(sizeW, sizeH);

        strokeWidth = STROKE_WIDTH * size; //描边宽度
        //圆心坐标
        ccX = sizeW / 2;
        ccY = sizeH / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(ccX, ccY, radius, circlePaint);
        Log.e("hejun", "onDraw: ");
        canvas.drawText("0", ccX, ccY - carNumberOffsetY, carNumberPaint);

        for (int i = 0; i < carsNumber; i++){
            drawCar(canvas, (float) (cars[i].getDistance() * m_to_dp), (float)cars[i].getAngle(), cars[i].getPort() - Base_port);
            Log.e("hejun", "onDraw: " + cars[i].getAngle() + " :" + cars[i].getPort());
        }

    }

    public void drawCar(Canvas canvas, float distance, float angle, int number){

        canvas.save();//锁定画布

        canvas.translate(ccX, ccY);//平移画布
        canvas.rotate(angle);//旋转画布

        //画圆,线,圆内的文字
        canvas.drawCircle(distance, 0, radius, circlePaint);
        canvas.drawLine(radius, 0, distance - radius, 0, linePaint);
        canvas.drawText(number + "", distance , - carNumberOffsetY, carNumberPaint);

        //旋转,使X到达Y的位置,画文字
        //canvas.rotate(90);
        canvas.drawText(distance / m_to_dp + "m", distance / 2, 0, textPaint);

        canvas.restore();//释放画布
    }

    public synchronized void update(RealPoint car){
        for (int i = 0; i < cars.length; i++) {
            if (car.getPort() == cars[i].getPort()){
                cars[i] = car;
            }
        }
        //重绘
        invalidate();
//        //非ui线程重绘
//        postInvalidate();
    }
}
