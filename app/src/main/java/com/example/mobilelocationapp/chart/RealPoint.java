package com.example.mobilelocationapp.chart;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;

public class RealPoint implements Serializable {
    private double x;
    private double y;
    private int port;

    public RealPoint(int port, double x, double y) {
        this.port = port;
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getPort() {return port;}

    public double getDistance(){
        double distance = Math.sqrt(x * x + y * y);
        //保留小数点后三位
        BigDecimal b = new BigDecimal(distance);
        distance = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return distance;
    }

    //得到的是角度
    public double getAngle(){
        double anglePI = Math.acos(x / getDistance());
        double angle = anglePI * 180 / Math.PI;//弧度转角度

        if (y < 0){
            angle = 360 - angle;
        }

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
