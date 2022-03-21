package com.example.mobilelocationapp.fzy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

//public class MyDrawView extends View {
//
//    private static final String TAG = "Draw";
//    Paint paint;
//    Path path;
//    float xPos;
//    float yPos;
//    //声明画笔
//    private Canvas my_canvas;
//    //声明位图
//    private Bitmap bitmap;
//
//    public MyDrawView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        //The Paint class holds the style and color information
//        //about how to draw geometries, text and bitmaps.
//        paint = new Paint();
//        path = new Path();
//        //设置抗锯齿
//        paint.setAntiAlias(true);
//        paint.setColor(Color.RED);
//        //设置粗线连接点
//        paint.setStrokeJoin(Paint.Join.ROUND);
//
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5f);
//
//        Log.e(TAG, "MyDrawView: " + getLeft() + " " + getRight());
//
//
//        //设置位图的宽高
//        bitmap = Bitmap.createBitmap(700, 800, Bitmap.Config.RGB_565);
//        bitmap.eraseColor(Color.GRAY);
//        my_canvas = new Canvas(bitmap);
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        Log.e(TAG, "onDraw: ");
//
//        //圆心的坐标
//        float x = xPos;
//        float y = yPos;
//        //圆半径
//        int radius;
//
////        bitmap.setWidth(100);
//
////        //设置位图的宽高
////        bitmap = Bitmap.createBitmap(700, 800, Bitmap.Config.RGB_565);
////        bitmap.eraseColor(Color.GRAY);
////        my_canvas = new Canvas(bitmap);
//
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//        paint.setColor(Color.BLACK);
//        canvas.drawCircle(100, 100, 10, paint);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        xPos = event.getX();
//        yPos = event.getY();
//
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                my_canvas.drawPoint(xPos, yPos, paint);
//                Log.e(TAG, "onTouchEvent: " + xPos + " " + yPos);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                path.lineTo(xPos, yPos);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//            default:
//                return false;
//        }
//        //是绘图效果生效
//        invalidate();
//        return true;
//    }
//}
