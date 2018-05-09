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
    private Sensor magneticSensor;


    CompassSensor(SensorManager sensorManager, CompassSensorListener listener){
        this.sensorManager=sensorManager;
        this.listener=listener;

        magneticSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(magneticSensor != null){
            registerListener();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){
            float degree = Math.round(event.values[0]);

            if(listener!=null) {
                listener.onCompassSensorChanged(degree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void registerListener(){
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }
    public void unregisterListener(){
        sensorManager.unregisterListener(this);
    }
}
