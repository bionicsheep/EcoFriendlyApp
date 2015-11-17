package tunca.tom.ecofriendlyapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class TripMonitor extends Service implements SensorEventListener, LocationListener,
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
    private ArrayList<Location> mLocations;

    //gps tracking
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int LOCATION_INTERVAL = 10000;
    private static final int FASTEST_LOCATION_INTERVAL = 5000;
    private static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private Location mStartLocation;

    public void onCreate(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TripMonitor");
        mWakeLock.acquire();

        buildGoogleApiClient();
        initializeLocation();
        initializeAccelorometer();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initializeLocation(){
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        mLocationRequest.setPriority(LOCATION_PRIORITY);

        mLocations = new ArrayList<>();
    }

    private void initializeAccelorometer(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        mAcceleration = 0.00f;
        mAccelerationCurrent = mSensorManager.GRAVITY_EARTH;
        mAccelerationLast = mSensorManager.GRAVITY_EARTH;
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
            if(mAcceleration > 3 && !tracking){
                Toast.makeText(getApplicationContext(), "gps tracking started",
                        Toast.LENGTH_LONG).show();
                startTracking();
            }
        }
    }

    private void startTracking(){
        tracking = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopTracking(){
        tracking = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mLocations.clear();
        mStartLocation = null;
    }

    @Override
    public void onLocationChanged(Location location){
        if(mStartLocation == null) {
            mStartLocation = location;
        }else{
            if(mLocations.size() <= LOC_SAMPLE_SIZE){
                mLocations.add(location);
            }
            else{
                checkIfTripStarted();
            }
        }
    }

    private void checkIfTripStarted(){
        int numPositiveResults = 0;
        for(Location loc : mLocations){
            Log.d("distance","" + mStartLocation.distanceTo(loc));
            if(mStartLocation.distanceTo(loc) >= MIN_DISTANCE_TRIGGER){

                numPositiveResults++;
            }
        }
        if(numPositiveResults >= MIN_POS_RESULTS){
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Trip started ")
                            .setContentText(numPositiveResults + " successes");
            mNotifyMgr.notify(001, mBuilder.build());
            Log.d("Trip", "trip succeeded due to POS_RESULTS being " + numPositiveResults);
            stopTracking();
        }else{
            Log.d("Trip","trip failed due to POS_RESULTS being " + numPositiveResults);
            stopTracking();
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
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        mWakeLock.release();
    }

    @Override
    public void onConnected(Bundle arg0) {
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
    }
}
