package com.example.marta.sensorapplication;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

public class MainActivity extends AppCompatActivity{

    public static final int MINIMUM_LIGHT=20;

    public static final String SHARED_PREFERENCES_X_COORD = "xcoord";
    public static final String SHARED_PREFERENCES_Y_COORD = "ycoord";
    public static final String SHARED_PREFERENCES_HOUR = "hour";
    public static final String SHARED_PREFERENCES_MINUTES = "minutes";
    public static final String SET_HOME="0";

    public static final int MILISECONDS_IN_A_DAY=86400*1000;

    ImageView ivCompass;
    ImageView ivArrow;
    ImageView ivHomeButton;
    ImageView ivClockButton;
    TextView tvHomeCoordinates;
    TextView tvDistance;

    private SensorManager sensorManager;
    private GPSTracker gpsTracker;
    private LocationManager locationManager;
    private CompassSensor compassSensor;

    private SharedPreferences sharedPreferences;
    private LightSensor lightSensor;

    private TimeHolder timeHolder;
    BitmapLoader bitmapLoader;

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

        lightSensor.registerListener();
        gpsTracker.startLocationUpdates();
        compassSensor.registerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightSensor.unregisterListener();
        gpsTracker.stopLocationUpdates();
        compassSensor.unregisterListener();
    }

    private void setOnHomeClick(){
        final AlertDialog.Builder alert= new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dom))
                .setPositiveButton(getResources().getString(R.string.tak), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationManager.setHomeCoordinateX(locationManager.getCurrentCoordinateX());
                        locationManager.setHomeCoordinateY(locationManager.getCurrentCoordinateY());

                        tvHomeCoordinates.setText("X: "+locationManager.getHomeCoordinateX()+", Y: "+locationManager.getHomeCoordinateY());
                        Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.zapisano), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM ,0,10);
                        toast.show();
                        tvDistance.setText(locationManager.formatDistance(locationManager.calculateDistance()));
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
            editor.putString(SHARED_PREFERENCES_X_COORD, Double.toString(locationManager.getHomeCoordinateX()));
            editor.putString(SHARED_PREFERENCES_Y_COORD, Double.toString(locationManager.getCurrentCoordinateY()));
            editor.putInt(SHARED_PREFERENCES_HOUR, timeHolder.getHour());
            editor.putInt(SHARED_PREFERENCES_MINUTES,timeHolder.getMinute());
            editor.apply();
        }
    }

    private void loadStateFomSharedPreferences(){
        if(sharedPreferences!=null){
            locationManager.setHomeCoordinateX(Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_X_COORD, SET_HOME)));
            locationManager.setHomeCoordinateY(Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_Y_COORD, SET_HOME)));
            timeHolder.setHour(sharedPreferences.getInt(SHARED_PREFERENCES_HOUR,0));
            timeHolder.setMinute(sharedPreferences.getInt(SHARED_PREFERENCES_MINUTES,0));
            tvHomeCoordinates.setText("X: "+locationManager.getHomeCoordinateX()+", Y: "+locationManager.getHomeCoordinateY());
        }
    }

    private void initializeLightListener(){

        final View view = this.getWindow().getDecorView().findViewById(android.R.id.content);

        LightSensorListener lightListener = new LightSensorListener() {
            @Override
            public void onLightSensorChange(float intensity) {

                if(intensity>MINIMUM_LIGHT) {
                    view.setBackgroundColor(view.getResources().getColor(R.color.backgroundLight));
                    tvHomeCoordinates.setTextColor(view.getResources().getColor(R.color.textDark));
                    tvDistance.setTextColor(view.getResources().getColor(R.color.textDark));
                    ivArrow.setColorFilter(Color.BLACK);
                    ivCompass.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.compass_dark_file),getApplicationContext()));
                }
                else{
                    view.setBackgroundColor(view.getResources().getColor(R.color.backgroundDark));
                    tvHomeCoordinates.setTextColor(view.getResources().getColor(R.color.textLight));
                    tvDistance.setTextColor(view.getResources().getColor(R.color.textLight));
                    ivArrow.setColorFilter(Color.rgb(200,35,26));
                    ivCompass.setImageBitmap(bitmapLoader.getBitmapFromAssets(getResources().getString(R.string.compass_file),getApplicationContext()));
                }
            }
        };
        lightSensor=new LightSensor(sensorManager, lightListener);
    }

    private void initializeCompassListener(){
        CompassSensorListener compassListener= new CompassSensorListener() {
            @Override
            public void onCompassSensorChanged(float degree) {

                RotateAnimation ra = new RotateAnimation(locationManager.getCurrentAzimuth(),-degree,Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                ra.setDuration(210);
                ra.setFillAfter(true);
                ivCompass.startAnimation(ra);
                locationManager.setCurrentAzimuth(-degree);

                float alpha = locationManager.calculateAngleToHome()+locationManager.getCurrentAzimuth();
                RotateAnimation ra2 = new RotateAnimation(locationManager.getCurrentHomeAngle(),alpha,Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                ra2.setDuration(210);
                ra2.setFillAfter(true);
                ivArrow.startAnimation(ra2);
                locationManager.setCurrentHomeAngle(alpha);
            }
        };
        compassSensor=new CompassSensor(sensorManager,compassListener);
    }

    private void initializeGPSTracker(){
        GPSTrackerListener gpsTrackerListener= new GPSTrackerListener() {
            @Override
            public void onGPSUpdate(double latitude, double longitude) {

                locationManager.setCurrentCoordinateX(latitude);
                locationManager.setCurrentCoordinateY(longitude);

                System.out.println(locationManager.formatDistance(locationManager.calculateDistance()));
                tvDistance.setText(locationManager.formatDistance(locationManager.calculateDistance()));

            }

            @Override
            public void onCoordinatesUpdate(double latitude, double longitude) {
                locationManager.setCurrentCoordinateX(latitude);
                locationManager.setHomeCoordinateY(longitude);
                tvDistance.setText(locationManager.formatDistance(locationManager.calculateDistance()));
            }
        };
        gpsTracker=new GPSTracker(gpsTrackerListener, getApplicationContext());
        tvDistance.setText(Double.toString(locationManager.calculateDistance()));
    }


    private void initialize(){
        bitmapLoader=BitmapLoader.getInstance();

        ivCompass =findViewById(R.id.iv_compass);


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
        locationManager=new LocationManager();

        initializeLightListener();
        initializeCompassListener();
        initializeGPSTracker();
    }

    public void timeNotification() {

        Calendar cal = Calendar.getInstance();
        Calendar dest = Calendar.getInstance();
        dest.set(Calendar.HOUR_OF_DAY, timeHolder.getHour());
        dest.set(Calendar.MINUTE, timeHolder.getMinute());
        dest.set(Calendar.SECOND, 0);

        long delta = dest.getTime().getTime() - cal.getTime().getTime();
        System.out.println(delta);

        if(delta<0){
            delta+=(MILISECONDS_IN_A_DAY);
        }

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
                createNotitification();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        }, delta);
    }

    private void createNotitification(){

        NotificationCompat.Builder notificationBuilder= new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getResources().getString(R.string.natychmiast_do_domu))
                //.setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, notificationBuilder.build());

    }
}
