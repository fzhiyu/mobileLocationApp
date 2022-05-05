package com.example.mobilelocationapp.chart;

import java.io.Serializable;
import java.util.ArrayList;

public class CarList implements Serializable {
    private ArrayList<RealPoint> realPointList = new ArrayList<>();
    private double target_x = 0, target_y = 0;

    private int port;

    public CarList(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public ArrayList<RealPoint> getRealPointList() {
        return realPointList;
    }

    public double getTarget_x() {
        return target_x;
    }

    public void setTarget_x(double target_x) {
        this.target_x = target_x;
    }

    public double getTarget_y() {
        return target_y;
    }

    public void setTarget_y(double target_y) {
        this.target_y = target_y;
    }

    public void addRealPoint(double x, double y){
        addRealPoint(new RealPoint(port, x, y));
    }

    public void addRealPoint(RealPoint realPoint){
        realPointList.add(realPoint);
    }

    /**
     * 单位 cm
     * @return
     */
    public ArrayList<Double> getXError(){
        ArrayList<Double> xErrorList = new ArrayList<>();
        int size = realPointList.size();
        for (int i = 0; i < size; i++) {
            double error_cm = (realPointList.get(i).getX() - target_x) * 100;//m转化为cm
            xErrorList.add(error_cm);
        }
        return xErrorList;
    }


    public ArrayList<Double> getYError(){
        ArrayList<Double> yErrorList = new ArrayList<>();
        int size = realPointList.size();
        for (int i = 0; i < size; i++) {
            double error_cm = (realPointList.get(i).getY() - target_y) * 100;
            yErrorList.add(error_cm);
        }
        return yErrorList;
    }

    public boolean isEmpty(){
        return realPointList.isEmpty();
    }

    public void clear(){
        realPointList.clear();
        target_x = 0;
        target_y = 0;
    }

}
