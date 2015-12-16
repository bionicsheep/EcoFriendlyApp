package tunca.tom.ecofriendlyapp;

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
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;

public class TripMonitor extends Service implements LocationListener,SensorEventListener,
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
    private int staleChecks = 0;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;

    private static final int LOCATION_INTERVAL_LOW = MINUTE * 10;
    private static final int LOCATION_INTERVAL_MED = SECOND * 60;
    private static final int LOCATION_INTERVAL_HIGH = SECOND * 10;

    private static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;


    private int updateUrgency = 2;
    private static final int URGENCY_HISTORY = 5;
    private ArrayList<Event> history = new ArrayList<Event>();

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

        initializeAccelorometer();
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
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LOCATION_INTERVAL_HIGH);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL_HIGH);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateRequestPriority(int urgency){

        int temp = updateUrgency;
        switch (urgency){
            case 0:
                Log.d("urgency update", "low");
                turnOnAccelerometer();
                mLocationRequest.setInterval(LOCATION_INTERVAL_LOW);
                break;
            case 1:
                Log.d("urgency update", "med");
                turnOnAccelerometer();
                mLocationRequest.setInterval(LOCATION_INTERVAL_MED);
                break;
            case 2:
                Log.d("urgency update", "high");
                turnOffAccelerometer();
                mLocationRequest.setInterval(LOCATION_INTERVAL_HIGH);
                break;
        }
    }

    private void turnOnAccelerometer(){
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        mAcceleration = 0.00f;
        mAccelerationCurrent = mSensorManager.GRAVITY_EARTH;
        mAccelerationLast = mSensorManager.GRAVITY_EARTH;
    }

    private void turnOffAccelerometer(){
        mSensorManager.unregisterListener(this);
    }

    private void addEntry(Event event){
        ContentValues mValues = new ContentValues();

        mValues.put(LocationHistoryDatabase.COL_1, event.getDate());
        mValues.put(LocationHistoryDatabase.COL_2, event.getTime());
        mValues.put(LocationHistoryDatabase.COL_3, event.getxCoor());
        mValues.put(LocationHistoryDatabase.COL_4, event.getyCoor());
        mValues.put(LocationHistoryDatabase.COL_5, event.getVelocity());
        mValues.put(LocationHistoryDatabase.COL_6, event.getAccuracy());

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
            double velocity = location.getSpeed();
            double accuracy = location.getAccuracy();

            Event event = new Event(date, time, latitude, longitude, velocity, accuracy);

            evaluateUniqueness(event);
            addEntry(event);

            Log.d("adding entry", "");
            Log.d("date", date);
            Log.d("time", time);
            Log.d("latitude", "" + latitude);
            Log.d("longitude", "" + longitude);
            Log.d("velocity", "" + velocity);
            Log.d("accuracy", "" + accuracy);

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

    private boolean evaluateUniqueness(Event event){
        boolean uniqueness = true;
        double difTotal = 0;

        if(history.size() == LOC_SAMPLE_SIZE) {
            for (Event mEvent : history) {
                double dif = distanceDifference(event, mEvent);

                difTotal += (dif);
                Log.d("dif", "" + dif);

            }
            Log.d("diftotal", "" + difTotal);


            if (difTotal >= 20) {
                uniqueness = true;
                staleChecks = 0;
            } else {
                staleChecks++;
                uniqueness = false;
            }

            Log.d("stalechecks", "" + staleChecks);
        }

        history.add(event);
        if(history.size() > URGENCY_HISTORY){
            history.remove(0);
        }

        return uniqueness;
    }

    private void evaluateUrgency(){

        if(staleChecks > 10){
            updateRequestPriority(0);
        }else if(staleChecks > 5){
            updateRequestPriority(1);
        }else if(staleChecks == 0){
            updateRequestPriority(2);
        }
    }

    private void initializeAccelorometer(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];

            mAccelerationLast = mAccelerationCurrent;

            mAccelerationCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelerationCurrent - mAccelerationLast;
            mAcceleration = mAcceleration * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if(mAcceleration > 3){
                updateRequestPriority(3);
            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        //nothign to see here
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
    public void onDestroy() {
        mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("service_enabled", false);
        mEditor.commit();

        Log.d("service", "service destroyed");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        mSensorManager.unregisterListener(this);
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

    public double distanceDifference(Event event1, Event event2) {
        float[] results = new float[1];
        Location.distanceBetween(event1.getxCoor(), event1.getyCoor(),
                event2.getxCoor(),event2.getyCoor(), results);

        return (double)results[0];
    }
}
