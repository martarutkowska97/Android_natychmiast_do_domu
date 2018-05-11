package com.example.marta.sensorapplication;

/**
 * Created by marta on 2018-05-08.
 */

public interface GPSTrackerListener {
    void onGPSUpdate(double latitude, double longitude);
    void onCoordinatesUpdate(double latitude, double longitude);
}
