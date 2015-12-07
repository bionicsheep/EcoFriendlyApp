package tunca.tom.ecofriendlyapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TripMonitor extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    //to keep service running when device off
    private PowerManager.WakeLock mWakeLock;

    //to track accelerometer
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mGravity;
    private float mAcceleration;
    private float mAccelerationCurrent;
    private float  mAccelerationLast;

    //trip start logic
    private boolean tracking = false;
    private static final int MIN_POS_RESULTS = 2;
    private static final int MIN_DISTANCE_TRIGGER = 60;
    private static final int LOC_SAMPLE_SIZE = 5;

    //gps tracking
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;

    private static final int LOCATION_INTERVAL_LOW = MINUTE * 10;
    private static final int LOCATION_INTERVAL_MED = MINUTE * 5;
    private static final int LOCATION_INTERVAL_HIGH = SECOND * 10;

    private static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private int updateUrgency = 0;

    //data storage
    private SQLiteDatabase mDatabase;
    private LocationHistoryDatabase mDatabaseHelper;

    //date and time stuff
    private Calendar mCalendar;

    //preferences
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public void onCreate(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TripMonitor");
        mWakeLock.acquire();

        initializePreferences();
        buildGoogleApiClient();
        initializeDatabase();
        initializeLocation();
    }

    private void initializePreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initializeDatabase(){
        mDatabaseHelper = new LocationHistoryDatabase(getApplicationContext());
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    private void initializeLocation(){
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LOCATION_INTERVAL_HIGH); //test
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL_HIGH);
        mLocationRequest.setPriority(LOCATION_PRIORITY);
    }

    private void updateRequestPriority(int urgency){
        updateUrgency = urgency;

        switch (updateUrgency){
            case 0:
                mLocationRequest.setInterval(LOCATION_INTERVAL_LOW);
                break;
            case 1:
                mLocationRequest.setInterval(LOCATION_INTERVAL_MED);
                break;
            case 2:
                mLocationRequest.setInterval(LOCATION_INTERVAL_HIGH);
                break;
        }
    }

    private void addEntry(String date, String time, double xPos, double yPos, float velocity){
        ContentValues mValues = new ContentValues();

        mValues.put(LocationHistoryDatabase.COL_1,date);
        mValues.put(LocationHistoryDatabase.COL_2,time);
        mValues.put(LocationHistoryDatabase.COL_3,xPos);
        mValues.put(LocationHistoryDatabase.COL_4,yPos);
        mValues.put(LocationHistoryDatabase.COL_5,velocity);

        mDatabase.insert(
                LocationHistoryDatabase.TABLE_NAME,
                null,
                mValues
        );
    }

    private void startTracking(){
        tracking = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopTracking(){
        tracking = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location){
        if(location != null){
            String date = getDate();
            String time = getTime();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float velocity = location.getSpeed();

            addEntry(date, time, latitude, longitude, velocity);

            Log.d("adding entry", "");
            Log.d("date", date);
            Log.d("time", time);
            Log.d("latitude", "" + latitude);
            Log.d("longitude", "" + longitude);
            Log.d("velocity", "" + velocity);

            evaluateUrgency();
        }else{
            //failed to get location
        }
    }

    private String getDate(){
        mCalendar = Calendar.getInstance();
        String month = String.format("%02d",(mCalendar.get(Calendar.MONTH) + 1));
        String day = String.format("%02d",mCalendar.get(Calendar.DAY_OF_MONTH));
        String year = String.format("%02d",mCalendar.get(Calendar.YEAR));

        String date = month + day + year;

        return date;
    }

    private String getTime(){
        mCalendar = Calendar.getInstance();
        String hour = String.format("%02d",mCalendar.get(Calendar.HOUR));
        String minute = String.format("%02d",mCalendar.get(Calendar.MINUTE));
        String second = String.format("%02d",mCalendar.get(Calendar.SECOND));

        String time = hour + minute + second;

        return time;
    }

    private void evaluateUrgency(){
        //TODO
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service","service started");
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("service_enabled",false);
        mEditor.commit();

        Log.d("service", "service destroyed");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        mWakeLock.release();
    }

    @Override
    public void onConnected(Bundle arg0) {
        startTracking();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        stopTracking();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
    }
}
