package com.example.mobilelocationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.icu.util.MeasureUnit;
import android.net.wifi.hotspot2.omadm.PpsMoParser;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class CanvasView extends View {
    private static final float STROKE_WIDTH = 1F / 256F; // 描边宽度

    private static final String SCALE_RULER = "1m";//标度尺
    public static float m_to_dp = 50;//表示用多少dp代表一米

    public static int paddingTop = 40;//距离顶部多少dp

    private Paint paint, textPaint;//画笔
    private Context mContext;//上下文引用

    private float textOffsetY;//文本的Y轴偏移量

    private int size;//控件最小边长
    private int sizeH, sizeW;//控件的宽高

    private int ccX, ccY; // 圆心坐标
    private float strokeWidth; //描边宽度
    private float radius; //圆的半径

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

        //初始化文字画笔
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textOffsetY = (textPaint.ascent() + textPaint.descent()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        radius = m_to_dp;
        canvas.save();
        canvas.translate(ccX, ccY);
        while (radius < size){
            RectF oval = new RectF(-radius, -radius, radius, radius);
            canvas.drawArc(oval, 0, 180, false, paint);
            //canvas.drawCircle(ccX, ccY, radius, paint);

            radius += m_to_dp;
        }
        canvas.restore();

        //画十字交叉线
        canvas.drawLine(ccX - (radius - m_to_dp), ccY, ccX + (radius - m_to_dp), ccY, paint);//横线
        canvas.drawLine(ccX, ccY, ccX, ccY + (radius - m_to_dp), paint);//竖线

        canvas.drawText(SCALE_RULER, ccX + (radius -  m_to_dp * (float)1.5), ccY, textPaint);//画标度尺

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //强制长宽一样
//        super.onMeasure(widthMeasureSpec, widthMeasureSpec / 2);
//        Log.e("hejun", "onMeasure: " + widthMeasureSpec + " " + heightMeasureSpec );
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //获取控件的边长
        sizeW = w;
        sizeH = h;
        size = Math.min(w / 2, h - paddingTop) ;

        strokeWidth = STROKE_WIDTH * size; //描边宽度
        //圆心坐标
        ccX = sizeW / 2;
        ccY = paddingTop;

    }

    public synchronized void update(){
        //重绘
        invalidate();
        //非ui线程重绘
        postInvalidate();
    }


}
