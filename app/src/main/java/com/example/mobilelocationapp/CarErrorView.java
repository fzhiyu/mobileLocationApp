package com.example.mobilelocationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CarErrorView extends View {
    private static final float STROKE_WIDTH = 1F / 256F; // 描边宽度

    private Paint circlePaint;//画车
    private Paint linePaint;//画线
    private Paint carNumberPaint;//画代表小车编号的文字
    private Paint textPaint;//画文字
    private Context mContext;//上下文引用

    private int size;//控件边长
    private int ccX, ccY; // 圆心坐标

    private float strokeWidth; //描边宽度
    private float radius = 25; //圆的半径

    private float carNumberOffsetY;//小车编号文本的Y轴偏移量
    private float textOffsetY;//文本的Y轴偏移量

    private float[] carsDistance;//小车的距离
    private float[] carsAngle;//小车的角度
    private int carsNumber;//小车的数量

    public CarErrorView(Context context) {
        this(context, null);
    }

    public CarErrorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, null, null);
    }

    public CarErrorView(Context context, @Nullable AttributeSet attrs, float[] distances, float[] angles){
        super(context, attrs);

        mContext = context;
        carsDistance = distances;
        carsAngle = angles;
        carsNumber = carsDistance.length;

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
        carNumberPaint.setTextSize(20);
        carNumberPaint.setTextAlign(Paint.Align.CENTER);

        carNumberOffsetY = (carNumberPaint.ascent() + carNumberPaint.descent()) / 2;

        //初始化文字画笔
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textOffsetY = (textPaint.ascent() + textPaint.descent()) / 2;

        //初始化线段画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);//新建画笔并设置画笔抗锯齿
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //强制长宽一样
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //获取控件的边长
        size = w;

        strokeWidth = STROKE_WIDTH * size; //描边宽度
        //圆心坐标
        ccX = size / 2;
        ccY = size / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(ccY, ccY, radius, circlePaint);
        canvas.drawText("1", ccX, ccY - carNumberOffsetY, carNumberPaint);

        for (int i = 0; i < carsNumber; i++){
            drawCar(canvas, carsDistance[i], carsAngle[i], i);
        }

//        int i = 0;
//        drawCar(canvas, carsDistance[i], carsAngle[i], i);
    }

    public void drawCar(Canvas canvas, float distance, float angle, int number){
        //得到相对的圆心坐标
        float x = distance * (float) Math.cos(angle);
        float y = - distance * (float) Math.sin(angle);

        canvas.save();//锁定画布

        canvas.translate(ccX, ccY);//平移画布
        canvas.rotate(270 - angle);//旋转画布

        //画圆,线,圆内的文字
        canvas.drawCircle(0, distance, radius, circlePaint);
        canvas.drawLine(0, radius, 0, distance - radius, linePaint);
        canvas.drawText(number + "", 0, distance - carNumberOffsetY, carNumberPaint);

        //旋转,使X到达Y的位置,画文字
        canvas.rotate(90);
        canvas.drawText(distance + "m" + angle + "°", distance / 2, 0, textPaint);

        canvas.restore();//释放画布
    }

    public synchronized void update(float[] distances, float[] angles){
        carsDistance = distances;
        carsAngle = angles;
        carsNumber = carsDistance.length;
        //重绘
        invalidate();
        //非ui线程重绘
        postInvalidate();
    }
}
