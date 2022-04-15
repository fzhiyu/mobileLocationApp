package com.example.mobilelocationapp.chart;

import com.example.mobilelocationapp.utils.Tools;

import java.io.Serializable;
import java.math.BigDecimal;

public class TargetPoint implements Serializable {

    private double x;
    private double y;

    public TargetPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        //保留小数点后三位
        return Tools.formatDecimal(x, 3);
    }

    public double getY() {
        return Tools.formatDecimal(y, 3);
    }

    @Override
    public String toString() {
        return "(" + getX() + " , " + getY() + ")";
    }


}
