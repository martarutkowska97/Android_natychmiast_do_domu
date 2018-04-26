package com.example.marta.sensorapplication;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private final String compassFile ="compass.png";
    private final String homeButtonFile="house.png";
    private final String clockFile="alarm-clock.png";
    private final String arrowFile="arrow1.png";
    private final String angryFaceFile="angry.png";

    public static final String DOM="Czy na pewno tu jest Twój dom?";
    public static final String TAK="TAK";
    public static final String NIE="NIE";
    public static final String ZAPISANO="Zapisano jako dom";

    public static final String SHARED_PREFERENCES_X_COORD = "xcoord";
    public static final String SHARED_PREFERENCES_Y_COORD = "ycoord";
    public static final String SHARED_PREFERENCES_HOUR = "hour";
    public static final String SHARED_PREFERENCES_MINUTES = "minutes";

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
    //Location location;
    SharedPreferences sharedPreferences;
    LigthSensor lightSensor;
    View view;

    int hour;
    int minute;


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
        float degree = Math.round(event.values[0]);
        RotateAnimation ra = new RotateAnimation(currentAzimuth,-degree,Animation.RELATIVE_TO_SELF,0.5f,
                    Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        ivCompass.startAnimation(ra);
        currentAzimuth=-degree;
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


    private Bitmap getBitmapFromAssets(String fileName){

        AssetManager am = getAssets();
        InputStream is = null;
        try{
            is = am.open(fileName);
        }catch(IOException e){
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    private void setOnHomeClick(){
        final AlertDialog.Builder alert= new AlertDialog.Builder(this)
                .setTitle(DOM)
                .setMessage("")
                .setPositiveButton(TAK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        homeCoordinateX = gpsTracker.getCurrentCoordinateX();
                        homeCoordinateY = gpsTracker.getCurrentCoordinateY();
                        //homeCoordinateX=62;
                        //homeCoordinateY=45;
                        tvHomeCoordinates.setText("X: "+homeCoordinateX+", Y: "+homeCoordinateY);
                        Toast toast = Toast.makeText(getApplicationContext(),ZAPISANO, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM ,0,10);
                        toast.show();
                        tvDistance.setText(gpsTracker.formatDistance(gpsTracker.calculateDistance()));
                        saveToSharedPreferences();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(NIE, null)
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

                setTime.setCurrentHour(hour);
                setTime.setCurrentMinute(minute);

                setTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfDay) {
                        hour = hourOfDay;
                        minute = minuteOfDay;
                    }
                });

                dialogBuilder.setView(popUpSetHour);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //
                        timeNotification();
                        hour = setTime.getCurrentHour();
                        minute = setTime.getCurrentMinute();
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
            editor.putInt(SHARED_PREFERENCES_HOUR, hour);
            editor.putInt(SHARED_PREFERENCES_MINUTES,minute);
            editor.apply();

        }
    }

    private void loadStateFomSharedPreferences(){
        if(sharedPreferences!=null){
            homeCoordinateX = Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_X_COORD, "-1"));
            homeCoordinateY = Double.parseDouble(sharedPreferences.getString(SHARED_PREFERENCES_Y_COORD, "-1"));
            hour=sharedPreferences.getInt(SHARED_PREFERENCES_HOUR,0);
            minute=sharedPreferences.getInt(SHARED_PREFERENCES_MINUTES,0);
            tvHomeCoordinates.setText("X: "+homeCoordinateX+", Y: "+homeCoordinateY);
        }
    }
    private void initialize(){
        ivCompass =findViewById(R.id.iv_compass);
        ivCompass.setImageBitmap(getBitmapFromAssets(compassFile));

        ivArrow=findViewById(R.id.iv_arrow);
        ivArrow.setImageBitmap(getBitmapFromAssets(arrowFile));

        ivHomeButton=findViewById(R.id.iv_home_button);
        ivHomeButton.setImageBitmap(getBitmapFromAssets(homeButtonFile));

        ivClockButton=findViewById(R.id.iv_clock_button);
        ivClockButton.setImageBitmap(getBitmapFromAssets(clockFile));

        sharedPreferences=this.getPreferences(Context.MODE_PRIVATE);
        tvHomeCoordinates=findViewById(R.id.test);
        tvDistance=findViewById(R.id.tv_distance);
        view=this.getWindow().getDecorView();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lightSensor=new LigthSensor(sensorManager,view, tvDistance,tvHomeCoordinates, ivArrow);
        gpsTracker = new GPSTracker(getApplicationContext(), tvDistance,ivArrow);
        //location = gpsTracker.getLocation();
        tvDistance.setText(Double.toString(gpsTracker.getDistance()));
    }

    public void timeNotification(){

        Calendar cal = Calendar.getInstance();
        Calendar dest = Calendar.getInstance();
        dest.set(Calendar.HOUR_OF_DAY, hour);
        dest.set(Calendar.MINUTE, minute);
        dest.set(Calendar.SECOND, 0);

        final long delta = dest.getTime().getTime()-cal.getTime().getTime();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(MainActivity.this);
                final View popUpSetHour=getLayoutInflater().inflate(R.layout.pop_up_do_domu,null);

                Button button=popUpSetHour.findViewById(R.id.button_juz_biegne);

                ImageView imageView=popUpSetHour.findViewById(R.id.iv_angry_face);
                imageView.setImageBitmap(getBitmapFromAssets(angryFaceFile));

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
