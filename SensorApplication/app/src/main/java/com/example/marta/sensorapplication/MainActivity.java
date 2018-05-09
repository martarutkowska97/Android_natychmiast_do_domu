package com.example.marta.sensorapplication;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    public static final int MINIMUM_LIGHT=20;

    public static final String SHARED_PREFERENCES_X_COORD = "xcoord";
    public static final String SHARED_PREFERENCES_Y_COORD = "ycoord";
    public static final String SHARED_PREFERENCES_HOUR = "hour";
    public static final String SHARED_PREFERENCES_MINUTES = "minutes";
    public static final String SET_HOME="?";

    public static double homeCoordinateX;
    public static double homeCoordinateY;

    ImageView ivCompass;
    ImageView ivArrow;
    ImageView ivHomeButton;
    ImageView ivClockButton;
    TextView tvHomeCoordinates;
    TextView tvDistance;

    private SensorManager sensorManager;
    private float currentAzimuth = 0f;
    private float currentHomeAngle = 0f;
    GPSTracker gpsTracker;

    CompassSensor compassSensor;

    SharedPreferences sharedPreferences;
    LightSensor lightSensor;
    View view;

    TimeHolder timeHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        if(savedInstanceState==null) {
            loadStateFomSharedPreferences();
        }
        setOnHomeClick();
        setOnClockClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        lightSensor.registerListener();
        gpsTracker.startLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        lightSensor.unregisterListener();
        gpsTracker.stopLocationUpdates();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float alpha = gpsTracker.calculateAngleToHome()+currentAzimuth;
        RotateAnimation ra2 = new RotateAnimation(currentHomeAngle,alpha,Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        ra2.setDuration(210);
        ra2.setFillAfter(true);
        ivArrow.startAnimation(ra2);
        currentHomeAngle = alpha;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setOnHomeClick(){
        final AlertDialog.Builder alert= new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dom))
                .setPositiveButton(getResources().getString(R.string.tak), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        homeCoordinateX = gpsTracker.getCurrentCoordinateX();
                        homeCoordinateY = gpsTracker.getCurrentCoordinateY();
                        tvHomeCoordinates.setText("X: "+homeCoordinateX+", Y: "+homeCoordinateY);
                        Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.zapisano), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM ,0,10);
                        toast.show();
                        tvDistance.setText(gpsTracker.formatDistance(gpsTracker.calculateDistance()));
                        saveToSharedPreferences();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.nie), null)
                .setCancelable(false);

        ivHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });
    }


    private void setOnClockClick(){
        ivClockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(MainActivity.this);
                final View popUpSetHour=getLayoutInflater().inflate(R.layout.pop_up_set_hour,null);

                final TimePicker setTime=popUpSetHour.findViewById(R.id.time_picker);
                Button button=popUpSetHour.findViewById(R.id.button_sethour);

                setTime.setCurrentHour(timeHolder.getHour());
                setTime.setCurrentMinute(timeHolder.getMinute());

                setTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfDay) {
                        timeHolder.setHour(hourOfDay);
                        timeHolder.setMinute(minuteOfDay);
                    }
                });

                dialogBuilder.setView(popUpSetHour);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeNotification();
                        timeHolder.setHour(setTime.getCurrentHour());
                        timeHolder.setMinute(setTime.getCurrentMinute());
                        saveToSharedPreferences();
                        dialog.dismiss();

                    }
                });
            }
        });
    }

    private void saveToSharedPreferences(){
        if(sharedPreferences!=null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARED_PREFERENCES_X_COORD, Double.toString(homeCoordinateX));
            editor.putString(SHARED_PREFERENCES_Y_COORD, Double.toString(homeCoordinateY));
            editor.putInt(SHARED_PREFERENCES_HOUR, timeHolder.getHour());
            editor.putInt(SHARED_PREFERENCES_MINUTES,timeHolder.getMinute());
            editor.apply();
        }
    }

    private void loadStateFomSharedPreferences(){
        if(sharedPreferences!=null){
            homeCoordinateX = Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_X_COORD, SET_HOME));
            homeCoordinateY = Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_Y_COORD, SET_HOME));
            timeHolder.setHour(sharedPreferences.getInt(SHARED_PREFERENCES_HOUR,0));
            timeHolder.setMinute(sharedPreferences.getInt(SHARED_PREFERENCES_MINUTES,0));
            tvHomeCoordinates.setText("X: "+homeCoordinateX+", Y: "+homeCoordinateY);
        }
    }

    private void initializeLightListener(){
        LightSensorListener lightListener = new LightSensorListener() {
            @Override
            public void onLightSensorChange(float intensity) {
                if(intensity>MINIMUM_LIGHT) {
                    view.setBackgroundColor(view.getResources().getColor(R.color.background));
                    tvHomeCoordinates.setTextColor(view.getResources().getColor(R.color.textDark));
                    tvDistance.setTextColor(view.getResources().getColor(R.color.textDark));
                    ivArrow.setColorFilter(Color.BLACK);
                }
                else{
                    view.setBackgroundColor(view.getResources().getColor(R.color.colorPrimary));
                    tvHomeCoordinates.setTextColor(view.getResources().getColor(R.color.textLight));
                    tvDistance.setTextColor(view.getResources().getColor(R.color.textLight));
                    ivArrow.setColorFilter(Color.rgb(200,35,26));
                }
            }
        };
        lightSensor=new LightSensor(sensorManager, lightListener);
    }

    private void initializeCompassListener(){
        CompassSensorListener compassListener= new CompassSensorListener() {
            @Override
            public void onCompassSensorChanged(float currentAzimuth, float degree) {
                RotateAnimation ra = new RotateAnimation(currentAzimuth,-degree,Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                ra.setDuration(210);
                ra.setFillAfter(true);
                ivCompass.startAnimation(ra);
            }
        };

        compassSensor=new CompassSensor(sensorManager,compassListener);

    }

    private void initialize(){
        BitmapLoader bitmapLoader=BitmapLoader.getInstance();

        ivCompass =findViewById(R.id.iv_compass);
        ivCompass.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.compass_file),this));

        ivArrow=findViewById(R.id.iv_arrow);
        ivArrow.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.arrow_file),this));

        ivHomeButton=findViewById(R.id.iv_home_button);
        ivHomeButton.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.home_button_file),this));

        ivClockButton=findViewById(R.id.iv_clock_button);
        ivClockButton.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.alarm_button_file),this));

        tvHomeCoordinates=findViewById(R.id.test);
        tvDistance=findViewById(R.id.tv_distance);

        sharedPreferences=this.getPreferences(Context.MODE_PRIVATE);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        timeHolder=new TimeHolder();

        initializeLightListener();
        initializeCompassListener();

        GPSTrackerListener gpsTrackerListener= new GPSTrackerListener() {
            @Override
            public void onGPSUpdate() {

            }
        };

        gpsTracker = new GPSTracker(getApplicationContext(), tvDistance,ivArrow, gpsTrackerListener);
        tvDistance.setText(Double.toString(gpsTracker.getDistance()));
    }

    public void timeNotification() {

        Calendar cal = Calendar.getInstance();
        Calendar dest = Calendar.getInstance();
        dest.set(Calendar.HOUR_OF_DAY, timeHolder.getHour());
        dest.set(Calendar.MINUTE, timeHolder.getMinute());
        dest.set(Calendar.SECOND, 0);

        final long delta = dest.getTime().getTime() - cal.getTime().getTime();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BitmapLoader bitmapLoader = BitmapLoader.getInstance();
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                final View popUpSetHour = getLayoutInflater().inflate(R.layout.pop_up_do_domu, null);

                Button button = popUpSetHour.findViewById(R.id.button_juz_biegne);

                ImageView ivAngryFace = popUpSetHour.findViewById(R.id.iv_angry_face);
                ivAngryFace.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.angry_face_file), getApplicationContext()));

                dialogBuilder.setView(popUpSetHour);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.setCancelable(false);
                dialog.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        }, delta);
    }
}
