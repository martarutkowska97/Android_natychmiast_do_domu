package com.example.marta.sensorapplication;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.TextView;

/**
 * Created by marta on 2018-04-24.
 */

public class LigthSensor implements SensorEventListener{

    public static final int MINIMUM_LIGHT=50;

    SensorManager sensorManager;
    Sensor lightSensor;
    float currentLight;
    View view;
    TextView tvDistance;
    TextView tvCoords;

    public LigthSensor(SensorManager sensorManager, View view, TextView tvDistance, TextView tvCoords){
        this.sensorManager=sensorManager;
        lightSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.view=view;
        view.setBackgroundColor(view.getResources().getColor(R.color.colorAccent));
        if(lightSensor != null){
            registerListener();
        }
        this.tvDistance=tvDistance;
        this.tvCoords=tvCoords;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            currentLight=event.values[0];
            //System.out.println(currentLight);
            if(currentLight>MINIMUM_LIGHT) {
                view.setBackgroundColor(view.getResources().getColor(R.color.colorAccent));
                tvCoords.setTextColor(view.getResources().getColor(R.color.textDark));
                tvDistance.setTextColor(view.getResources().getColor(R.color.textDark));
            }
            else{
                view.setBackgroundColor(view.getResources().getColor(R.color.colorPrimary));
                tvCoords.setTextColor(view.getResources().getColor(R.color.textLight));
                tvDistance.setTextColor(view.getResources().getColor(R.color.textLight));
            }
            //System.out.println(currentLight);


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerListener(){
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(){
        sensorManager.unregisterListener(this);
    }
}
