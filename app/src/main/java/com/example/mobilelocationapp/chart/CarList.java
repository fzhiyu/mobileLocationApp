package com.example.mobilelocationapp.chart;

import java.io.Serializable;
import java.util.ArrayList;

public class CarList implements Serializable {
    private ArrayList<RealPoint> realPointList = new ArrayList<>();
    private ArrayList<TargetPoint> targetPointList = new ArrayList<>();

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

    public ArrayList<TargetPoint> getTargetPointList() {
        return targetPointList;
    }


    public void addRealPoint(double x, double y){
        addRealPoint(new RealPoint(port, x, y));
    }

    public void addRealPoint(RealPoint realPoint){
        realPointList.add(realPoint);
    }


    public void addTargetPoint(double x, double y){
        addTargetPoint(new TargetPoint(x, y));
    }

    public void addTargetPoint(TargetPoint targetPoint){
        targetPointList.add(targetPoint);
    }

    /**
     * 单位 cm
     * @return
     */
    public ArrayList<Double> getXError(){
        ArrayList<Double> xErrorList = new ArrayList<>();
        int min = Math.min(realPointList.size(), targetPointList.size());
        for (int i = 0; i < min; i++) {
            double error_cm = (realPointList.get(i).getX() - targetPointList.get(i).getX()) * 100;//m转化为cm
            xErrorList.add(error_cm);
        }
        return xErrorList;
    }


    public ArrayList<Double> getYError(){
        ArrayList<Double> yErrorList = new ArrayList<>();
        int min = Math.min(realPointList.size(), targetPointList.size());
        for (int i = 0; i < min; i++) {
            double error_cm = (realPointList.get(i).getY() - targetPointList.get(i).getY()) * 100;
            yErrorList.add(error_cm);
        }
        return yErrorList;
    }

    public boolean isEmpty(){
        return realPointList.isEmpty() || targetPointList.isEmpty();
    }

    public void clear(){
        realPointList.clear();
        targetPointList.clear();
    }

}
