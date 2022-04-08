package com.example.mobilelocationapp.fzy;

public class Car {
    private float x;
    private float y;
    private double length;
    private double radius;
    private String checkedRadio;
    private int port = -1;

    public Car(float x, float y, int port) {
        this.x = x;
        this.y = y;
        this.port = port;
    }

    public Car(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Car(float x, float y, String checkedRadio) {
        this.x = x;
        this.y = y;
        this.checkedRadio = checkedRadio;
    }

    public Car(float x, float y, double length, double radius) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.radius = radius;
    }

    public Car(float x, float y, double length, double radius, String checkedRadio) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.radius = radius;
        this.checkedRadio = checkedRadio;
    }

    public Car() {
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getCheckedRadio() {
        return checkedRadio;
    }

    public void setCheckedRadio(String checkedRadio) {
        this.checkedRadio = checkedRadio;
    }

    @Override
    public String toString() {
        return "Car{" +
                "x=" + x +
                ", y=" + y +
                ", length=" + length +
                ", radius=" + radius +
                ", checkedBox='" + checkedRadio + '\'' +
                '}';
    }
}
