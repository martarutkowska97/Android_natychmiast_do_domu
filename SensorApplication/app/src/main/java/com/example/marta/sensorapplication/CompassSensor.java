package com.example.marta.sensorapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by marta on 2018-04-27.
 */

public class CompassSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private CompassSensorListener listener;

    private float currentAzimuth = 0f;
    private float degree=0f;


    public CompassSensor(SensorManager sensorManager, CompassSensorListener listener){
        this.sensorManager=sensorManager;
        this.listener=listener;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            degree = Math.round(event.values[0]);
            currentAzimuth=-degree;

            if(listener!=null) {
                listener.onCompassSensorChanged(currentAzimuth,degree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
