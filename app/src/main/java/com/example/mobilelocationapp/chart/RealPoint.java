package com.example.mobilelocationapp.chart;

import androidx.annotation.NonNull;

import java.math.BigDecimal;

public class RealPoint {
    private double x;
    private double y;

    public RealPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDistance(){
        double distance = Math.sqrt(x * x + y * y);
        //保留小数点后三位
        BigDecimal b = new BigDecimal(distance);
        distance = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return distance;
    }

    //得到的是角度
    public double getAngle(){
        double anglePI = Math.atan(y / x);
        double angle = anglePI * 180 / Math.PI;//弧度转角度

        //保留小数点后三位
        BigDecimal b = new BigDecimal(angle);
        angle = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

        return angle;
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + x + " , " + y + ")";
    }
}
