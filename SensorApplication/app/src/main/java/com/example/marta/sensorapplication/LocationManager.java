package com.example.marta.sensorapplication;

import java.text.DecimalFormat;

public class LocationManager {

    private double homeCoordinateX;
    private double homeCoordinateY;
    private double currentCoordinateX;
    private double currentCoordinateY;

    private float currentAzimuth;
    private float currentHomeAngle;

    public static final float R=6371000; //radius of the Earth in meters

    LocationManager(){

    }

    public double getHomeCoordinateX() {
        return homeCoordinateX;
    }

    public double getHomeCoordinateY() {
        return homeCoordinateY;
    }

    public double getCurrentCoordinateX() {
        return currentCoordinateX;
    }

    public double getCurrentCoordinateY() {
        return currentCoordinateY;
    }

    public float getCurrentAzimuth() {
        return currentAzimuth;
    }

    public float getCurrentHomeAngle() {
        return currentHomeAngle;
    }

    public void setHomeCoordinateX(double homeCoordinateX) {
        this.homeCoordinateX = homeCoordinateX;
    }

    public void setHomeCoordinateY(double homeCoordinateY) {
        this.homeCoordinateY = homeCoordinateY;
    }

    public void setCurrentCoordinateX(double currentCoordinateX) {
        this.currentCoordinateX = currentCoordinateX;
    }

    public void setCurrentCoordinateY(double currentCoordinateY) {
        this.currentCoordinateY = currentCoordinateY;
    }

    public void setCurrentAzimuth(float currentAzimuth) {
        this.currentAzimuth = currentAzimuth;
    }

    public void setCurrentHomeAngle(float currentHomeAngle) {
        this.currentHomeAngle = currentHomeAngle;
    }



    public double calculateDistance(){

        double coordDiffX=Math.toRadians(homeCoordinateX - currentCoordinateX);
        double coordDiffY=Math.toRadians(homeCoordinateY - currentCoordinateY);

        double a = Math.sin(coordDiffX/2)*Math.sin(coordDiffX/2)+
                Math.cos(Math.toRadians(currentCoordinateX))*Math.cos(Math.toRadians(homeCoordinateX))
                        *Math.sin(coordDiffY/2)*Math.sin(coordDiffY/2);

        double c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        return R*c;
    }

    public float calculateAngleToHome(){
        return Math.round((Math.atan2(homeCoordinateY-currentCoordinateY, homeCoordinateX-currentCoordinateX))*180/Math.PI);
    }

    public double calculateAngle(){
        return Math.atan2(homeCoordinateX - currentCoordinateX, homeCoordinateY - currentCoordinateY);
    }

    public String formatDistance(double distance){

        DecimalFormat decimalFormat=new DecimalFormat("#0.00");
        if(distance>1000){
            return decimalFormat.format(distance/1000)+" km";
        }
        else {
            return decimalFormat.format(distance)+" m";
        }
    }
}
