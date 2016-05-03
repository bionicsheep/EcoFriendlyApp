package tunca.tom.ecofriendlyapp;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;

public class TripDataRecorder extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    //to keep service running when device off
    private PowerManager.WakeLock mWakeLock;

    //google api client
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //data storage
    private SQLiteDatabase mDatabase;

    //frequency for updates
    int intervalFrequency = LOCATION_INTERVAL_HIGH;

    //time constants
    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;

    private static final int LOCATION_INTERVAL_LOW = MINUTE * 10;
    private static final int LOCATION_INTERVAL_MED = SECOND * 60;
    private static final int LOCATION_INTERVAL_HIGH = SECOND * 10;

    private static final int LOC_HISTORY_BUFFER = 5;
    private static final int MIN_TOTAL_CHANGE = 60; //meters
    private static final int ALLOW_CENTER_DIFFERENCE = MIN_TOTAL_CHANGE / (LOC_HISTORY_BUFFER);

    //date and time stuff
    private Calendar mCalendar;

    //urgency and history evaluation
    private static final int MAX_INACCURACY = 80; //meters
    private ArrayList<Event> history = new ArrayList<>();
    private int staleChecks = 1;

    //accelerometer stuff
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float mAcceleration;
    private float mAccelerationCurrent;
    private float  mAccelerationLast;

    @Override
    public void onCreate(){
        initializeWakeLock();
        buildGoogleApiClient();
        initializeDatabase();
        initializeAccelorometer();
        initializeLocation(intervalFrequency);

        //when location actually starts tracking
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intervalFrequency = intent.getIntExtra("intervalFrequency", LOCATION_INTERVAL_HIGH);

        Log.d("TripDataRecorder","service started " + intervalFrequency);

        return START_REDELIVER_INTENT; //got the stupid crash bug fixed
    }

    @Override
    public void onConnected(Bundle bundle) {
        startTracking();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopTracking();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //do nothing
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //googles method with some tweaking
    public void onLocationChanged(Location location) {
        if(location != null){
            String date = getDate();
            String time = getTime();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double velocity = location.getSpeed();
            double accuracy = location.getAccuracy();

            Log.d("new location", "");
            Log.d("date", date);
            Log.d("time", time);
            Log.d("latitude", "" + latitude);
            Log.d("longitude", "" + longitude);
            Log.d("velocity", "" + velocity);
            Log.d("accuracy", "" + accuracy);

            //if point is too inaccurate to trust
            if(accuracy > MAX_INACCURACY){
                return;
            }

            Event event = new Event(date, time, latitude, longitude, velocity, accuracy);

            evaluateUniqueness(event);
            evaluateUrgency();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] mGravity = event.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];

            mAccelerationLast = mAccelerationCurrent;

            mAccelerationCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelerationCurrent - mAccelerationLast;
            mAcceleration = mAcceleration * 0.9f + delta;
            if (mAcceleration > 3) {
                updateRequestPriority(LOCATION_INTERVAL_HIGH);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing to see here
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initializeWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TripDataRecorder");
        mWakeLock.acquire();
    }

    public void onDestroy() {
        Log.d("TripDataRecorder", "service destroyed");

        stopTracking();
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if(mWakeLock != null){
            mWakeLock.release();
        }
    }

    private void initializeDatabase(){
        LocationHistoryDatabase mDatabaseHelper = new LocationHistoryDatabase(getApplicationContext());
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    private void initializeAccelorometer(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(intervalFrequency == LOCATION_INTERVAL_LOW){
            turnOnAccelerometer();
        }
    }

    private void initializeLocation(int intervalFrequency){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(intervalFrequency);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL_HIGH);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startTracking(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private String getDate(){
        mCalendar = Calendar.getInstance();
        String month = String.format("%02d",(mCalendar.get(Calendar.MONTH) + 1));
        String day = String.format("%02d",mCalendar.get(Calendar.DAY_OF_MONTH));
        String year = String.format("%02d",mCalendar.get(Calendar.YEAR));

        return (month + day + year);
    }

    private String getTime(){
        mCalendar = Calendar.getInstance();
        String hour = String.format("%02d",mCalendar.get(Calendar.HOUR));
        String minute = String.format("%02d",mCalendar.get(Calendar.MINUTE));
        String second = String.format("%02d",mCalendar.get(Calendar.SECOND));

        return (hour + minute + second);
    }

    private void addEntry(Event event){
        ContentValues mValues = new ContentValues();

        mValues.put(LocationHistoryDatabase.COL_1, event.getDate());
        mValues.put(LocationHistoryDatabase.COL_2, event.getTime());
        mValues.put(LocationHistoryDatabase.COL_3, event.getLatitude());
        mValues.put(LocationHistoryDatabase.COL_4, event.getLongitude());
        mValues.put(LocationHistoryDatabase.COL_5, event.getVelocity());
        mValues.put(LocationHistoryDatabase.COL_6, event.getAccuracy());

        mDatabase.insert(
                LocationHistoryDatabase.TABLE_NAME,
                null,
                mValues
        );
    }

    private void stopTracking(){
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void evaluateUniqueness(Event event){
        double minX, minY, maxX, maxY;
        double centerX, centerY;
        double weightedCenterX, weightedCenterY;

        //if history buffer full
        if(history.size() == LOC_HISTORY_BUFFER) {
            minX = getMinX();
            minY = getMinY();
            maxX = getMaxX();
            maxY = getMaxY();
            centerX = (maxX+minX)/2.0;
            centerY = (maxY+minY)/2.0;
            weightedCenterX = getWeightedCenterX();
            weightedCenterY = getWeightedCenterY();

            Log.d("centerx",""+centerX);
            Log.d("centery",""+centerY);

            Log.d("weicenterx",""+weightedCenterX);
            Log.d("weicentery",""+weightedCenterY);

            //total dx or dy must be greater than MIN_TOTAL_CHANGE
            Log.d("rms dx and dy","" + Math.sqrt((Math.pow(distanceDifferenceX(minX, maxX),2) + (Math.pow(distanceDifferenceY(minY, maxY),2)))));
            Log.d("must be greater than","" + MIN_TOTAL_CHANGE);
            if ((distanceDifferenceX(minX, maxX) + distanceDifferenceY(minY, maxY)) > MIN_TOTAL_CHANGE) {
                //make sure outlier isn't the cause
                Log.d("diff of centers","" + (distanceDifferenceX(centerX, weightedCenterX) +
                        distanceDifferenceY(centerY, weightedCenterY)));
                Log.d("must be less than","" + ALLOW_CENTER_DIFFERENCE);
                if (((distanceDifferenceX(centerX, weightedCenterX) + distanceDifferenceY(centerY, weightedCenterY)) < ALLOW_CENTER_DIFFERENCE)) {
                    //unique 5 enter middle point
                    Event uniqueEvent = history.get((int)LOC_HISTORY_BUFFER/2);
                    addEntry(uniqueEvent);
                    Log.d("adding unique entry", "new event");
                    staleChecks = 0;
                }
            }
            //toss first and add new point
            history.remove(0);
            staleChecks++;
        }
        history.add(event);
    }

    private double getMinX(){
        double min = history.get(0).getLongitude();
        for(int x = 1; x < history.size(); x++){
            double new_min = history.get(x).getLongitude();
            if(min > new_min){
                min = new_min;
            }
        }
        Log.d("minx",""+min);
        return min;
    }

    private double getMinY(){
        double min = history.get(0).getLatitude();
        for(int x = 1; x < history.size(); x++){
            double new_min = history.get(x).getLatitude();
            if(min > new_min){
                min = new_min;
            }
        }
        Log.d("miny",""+min);
        return min;
    }

    private double getMaxX(){
        double max = history.get(0).getLongitude();
        for(int x = 1; x < history.size(); x++){
            double new_max = history.get(x).getLongitude();
            if(max < new_max){
                max = new_max;
            }
        }
        Log.d("maxx",""+max);
        return max;
    }

    private double getMaxY(){
        double max = history.get(0).getLatitude();
        for(int x = 1; x < history.size(); x++){
            double new_max = history.get(x).getLatitude();
            if(max < new_max){
                max = new_max;
            }
        }
        Log.d("maxy",""+max);
        return max;
    }

    private double getWeightedCenterX(){
        double WeightedCenterX = 0;
        for(int x = 0; x < history.size(); x++){
            WeightedCenterX += history.get(x).getLongitude();
        }
        WeightedCenterX = (WeightedCenterX / (double)history.size());
        return WeightedCenterX;
    }

    private double getWeightedCenterY(){
        double WeightedCenterY = 0;
        for(int x = 0; x < history.size(); x++){
            WeightedCenterY += history.get(x).getLatitude();
        }
        WeightedCenterY = (WeightedCenterY / (double)history.size());
        return WeightedCenterY;
    }

    private void evaluateUrgency(){
        if(staleChecks == 0){
            updateRequestPriority(LOCATION_INTERVAL_HIGH);
        }else if(staleChecks > 5){
            if(intervalFrequency != LOCATION_INTERVAL_HIGH){
                updateRequestPriority(LOCATION_INTERVAL_MED);
            }
            else if(intervalFrequency != LOCATION_INTERVAL_MED){
                updateRequestPriority(LOCATION_INTERVAL_LOW);
            }
        }
    }

    private void updateRequestPriority(int urgency){
        Intent intent = new Intent("tunca.tom.ecofriendlyapp.RESTART_LOC_TRACKING");
        intent.putExtra("intervalFrequency", urgency);
        sendBroadcast(intent);
    }

    private void turnOnAccelerometer(){
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        mAcceleration = 0.00f;
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH;
        mAccelerationLast = SensorManager.GRAVITY_EARTH;
    }

    public double distanceDifferenceX(double x1, double x2) {
        float[] results = new float[1];
        Location.distanceBetween(0, x1, 0, x2, results);

        return (double)results[0];
    }

    public double distanceDifferenceY(double y1, double y2) {
        float[] results = new float[1];
        Location.distanceBetween(y1, 0, y2, 0, results);

        return (double)results[0];
    }
}