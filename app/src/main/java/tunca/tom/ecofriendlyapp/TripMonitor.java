package tunca.tom.ecofriendlyapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class TripMonitor extends Service implements SensorEventListener, LocationListener{

    //to keep service running when device off
    private PowerManager.WakeLock mWakeLock;

    //to track accelerometer
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mGravity;
    private float mAcceleration;
    private float mAccelerationCurrent;
    private float  mAccelerationLast;

    //should check gps
    private boolean tracking = false;

    //gps tracking
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    public void onCreate(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();

        initializeAccelorometer();
        initializeLocationSensors();
    }

    private void initializeAccelorometer(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        mAcceleration = 0.00f;
        mAccelerationCurrent = mSensorManager.GRAVITY_EARTH;
        mAccelerationLast = mSensorManager.GRAVITY_EARTH;
    }

    private void initializeLocationSensors(){
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        Log.i("Message: ", "Location changed, " + location.getAccuracy() + " , " + location.getLatitude() + "," + location.getLongitude());
        Toast.makeText(getApplicationContext(), ("Location changed, " + location.getAccuracy() + " , " + location.getLatitude() + "," + location.getLongitude()),
                Toast.LENGTH_LONG).show();

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                trackLocation();
            }
        }
    }

    private void trackLocation(){
        tracking = true;
        Toast.makeText(getApplicationContext(), "tracking location",
                Toast.LENGTH_LONG).show();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 25, this);
            Log.d("requesting","dfdsfsd");
        } catch (SecurityException ex){
            //failed to get location permission
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
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        mWakeLock.release();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
