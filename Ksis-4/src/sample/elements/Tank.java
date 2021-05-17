package sample.elements;

import java.io.Serializable;

public class Tank implements Serializable {
    private double x;
    private double y;
    private boolean isInjured;

    public Tank(){

    }
    public Tank(double x, double y){
        this.x = x;
        this.y = y;
        isInjured = false;
    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isInjured() {
        return isInjured;
    }

    public void setInjured(boolean injured) {
        isInjured = injured;
    }
}
