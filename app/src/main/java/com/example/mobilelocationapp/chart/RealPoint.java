package com.example.mobilelocationapp.chart;

import androidx.annotation.NonNull;

import com.example.mobilelocationapp.utils.Tools;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.experimental.Tolerate;

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
        return Tools.formatDecimal(x, 3);
    }

    public double getY() {
        return Tools.formatDecimal(y, 3);
    }

    public int getPort() {return port;}

    public double getDistance(){
        double distance = Math.sqrt(x * x + y * y);
        //保留小数点后三位
        return Tools.formatDecimal(distance, 3);
    }

    //得到的是角度
    public double getAngle(){
        double anglePI = Math.acos(x / getDistance());
        double angle = anglePI * 180 / Math.PI;//弧度转角度

        if (y < 0){
            angle = 360 - angle;
        }

//        //保留小数点后三位
//        BigDecimal b = new BigDecimal(angle);
//        angle = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//
//        return angle;
        return Tools.formatDecimal(angle, 3);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + getX() + " , " + getY() + ")";
    }
}
