package com.example.marta.sensorapplication;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by marta on 2018-04-24.
 */

public class LightSensor implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private LightSensorListener lightSensorListener;


    public LightSensor(SensorManager sensorManager, LightSensorListener lightSensorListener){
        this.sensorManager=sensorManager;
        lightSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor != null){
            registerListener();
        }
        this.lightSensorListener=lightSensorListener;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            float currentLight=event.values[0];
            if(lightSensorListener!=null) {
                lightSensorListener.onLightSensorChange(currentLight);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void registerListener(){
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(){
        sensorManager.unregisterListener(this);
    }

}

