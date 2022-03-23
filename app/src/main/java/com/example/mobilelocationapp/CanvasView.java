package com.example.mobilelocationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.MeasureUnit;
import android.net.wifi.hotspot2.omadm.PpsMoParser;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CanvasView extends View {
    private static final float STROKE_WIDTH = 1F / 256F; // 描边宽度

    private Paint paint;//画笔
    private Context mContext;//上下文引用

    private int size;//控件边长

    private int ccX, ccY; // 圆心坐标
    private float strokeWidth; //描边宽度
    private float radius; //第一个圆的半径

    public CanvasView(Context context){
        this(context, null);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        //初始化画笔
        init();
    }

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//新建画笔并设置画笔抗锯齿
        /**
         * 设置画笔样式为描边
         *
         * 画笔样式分三种：
         * 1.Paint.Style.STROKE：描边
         * 2.Paint.Style.FILL_AND_STROKE：描边并填充
         * 3.Paint.Style.FILL：填充
         */
        paint.setStyle(Paint.Style.STROKE);

        //设置画笔颜色为浅灰色
        paint.setColor(Color.LTGRAY);

        /**
         * 设置描边的粗细，单位：像素px
         * 注意：当setStrokeWidth(0)的时候描边宽度并不为0而是只占一个像素
         */
        paint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制圆环
        while (radius > 30){
            canvas.drawCircle(ccX,  ccY, radius, paint);
            //半径减30px
            radius -= 30;
        }

        //画十字交叉线
        canvas.drawLine(0, ccY, size, ccY, paint);
        canvas.drawLine(ccX, 0, ccX, size, paint);

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
        //圆的半径
        radius = size / 2;
    }

    public synchronized void update(){
        //重绘
        invalidate();
        //非ui线程重绘
        postInvalidate();
    }


}
