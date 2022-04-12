package com.example.mobilelocationapp.chart;

import java.io.Serializable;

public class TargetPoint implements Serializable {

    private Double X;
    private Double Y;

    public TargetPoint(Double x, Double y) {
        this.X = x;
        this.Y = y;
    }

    public Double getX() {
        return X;
    }

    public void setX(Double x) {
        X = x;
    }

    public Double getY() {
        return Y;
    }

    public void setY(Double y) {
        Y = y;
    }
}
