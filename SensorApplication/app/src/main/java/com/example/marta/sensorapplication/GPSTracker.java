package com.example.marta.sensorapplication;

/**
 * Created by marta on 2018-04-24.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;

/**
 * Created by shiva on 8/4/17.
 */

public class GPSTracker{

    Context context;
    public static double currentCoordinateX;
    public static double currentCoordinateY;
    public static double distance;

    final TextView tvDistance;
    final ImageView ivArrow;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;

    GPSTrackerListener gpsTrackerListener;

    //distance liczony z haversine formula

    public static final float R=6371000; //metry promień ziemi


    public GPSTracker(Context context,final TextView tvDistance, ImageView ivArrow, GPSTrackerListener gpsTrackerListener) {
        super();
        this.context = context;
        this.tvDistance=tvDistance;
        this.ivArrow=ivArrow;
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(context);
        getLocation();

        this.gpsTrackerListener=gpsTrackerListener;


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentCoordinateX = location.getLatitude();
                    currentCoordinateY = location.getLongitude();
                    distance = calculateDistance();
                    tvDistance.setText(formatDistance(distance));
                }
            }

        };

        startLocationUpdates();
    }

    public double getCurrentCoordinateX(){
        return currentCoordinateX;
    }
    public double getCurrentCoordinateY(){
        return currentCoordinateY;
    }
    public double getDistance(){
        return distance;
    }


    public void getLocation(){
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //return null;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                currentCoordinateX=location.getLatitude();
                                currentCoordinateY=location.getLongitude();
                                distance= calculateDistance();
                                tvDistance.setText(formatDistance(distance));
                                //Toast.makeText(context,"Zaaktualizowano",Toast.LENGTH_SHORT).show();

                                double angle = Math.atan2(MainActivity.homeCoordinateX-currentCoordinateX,MainActivity.homeCoordinateY-currentCoordinateY);
                                RotateAnimation ra = new RotateAnimation(0,(float)-angle, Animation.RELATIVE_TO_SELF,0.5f,
                                        Animation.RELATIVE_TO_SELF,0.5f);
                                ra.setDuration(210);
                                ra.setFillAfter(true);
                                ivArrow.startAnimation(ra);
                            }
                        });
    }

    public void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //return null;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public double calculateDistance(){

        double coordDiffX=Math.toRadians(MainActivity.homeCoordinateX - currentCoordinateX);
        double coordDiffY=Math.toRadians(MainActivity.homeCoordinateY - currentCoordinateY);

        double a = Math.sin(coordDiffX/2)*Math.sin(coordDiffX/2)+
                Math.cos(Math.toRadians(currentCoordinateX))*Math.cos(Math.toRadians(MainActivity.homeCoordinateX))
                *Math.sin(coordDiffY/2)*Math.sin(coordDiffY/2);

        double c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double distance=R*c;

        return distance;
    }

    public float calculateAngleToHome(){
        return Math.round((Math.atan2(MainActivity.homeCoordinateY-currentCoordinateY, MainActivity.homeCoordinateX-currentCoordinateX))*180/Math.PI);
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
