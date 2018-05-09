package com.example.marta.sensorapplication;

/**
 * Created by marta on 2018-04-24.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class GPSTracker{

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private GPSTrackerListener gpsTrackerListener;


    GPSTracker(GPSTrackerListener gpsTrackerListener, Context context) {
        super();
        this.context=context;
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(context);
        getLocation();

        this.gpsTrackerListener=gpsTrackerListener;


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
//                    currentCoordinateX = location.getLatitude();
//                    currentCoordinateY = location.getLongitude();
//                    distance = calculateDistance();
                }
            }

        };

        startLocationUpdates();
    }


    public void getLocation(){
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //return null;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                gpsTrackerListener.onGPSUpdate(location.getLatitude(), location.getLongitude());
                            }
                        });
    }

    public void startLocationUpdates() {
        LocationRequest locationRequest;
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //return null;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
