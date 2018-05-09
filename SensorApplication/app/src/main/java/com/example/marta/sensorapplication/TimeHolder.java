package com.example.marta.sensorapplication;

/**
 * Created by marta on 2018-04-27.
 */

public class TimeHolder {

    private int hour;
    private int minute;

    TimeHolder(){
        this.hour=0;
        this.minute=0;
    }
    public int getHour(){
        return hour;
    }
    public int getMinute(){
        return minute;
    }
    public void setHour(int hour){
        this.hour=hour;
    }
    public void setMinute(int minute){
        this.minute=minute;
    }
}
