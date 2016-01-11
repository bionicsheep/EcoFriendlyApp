package tunca.tom.ecofriendlyapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class TripMonitor extends Service{

    //to keep service running when device off
    private PowerManager.WakeLock mWakeLock;

    //time constants
    private static final int SECOND = 1000;
    private static final int LOCATION_INTERVAL_HIGH = SECOND * 10;

    //preferences
    private SharedPreferences mSharedPreferences;

    //BroadCast Reciever
    BroadcastReceiver x;

    public void onCreate(){
        initializeWakeLock();
        initializePreferences();
        initializeListener();
        initializeDataRecorder();
    }

    private void initializeWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TripMonitor");
        mWakeLock.acquire();
    }

    private void initializePreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initializeDataRecorder(){
        startTripDataRecorder(LOCATION_INTERVAL_HIGH);
    }

    private void initializeListener(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tunca.tom.ecofriendlyapp.RESTART_LOC_TRACKING");

        x = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int intervalFrequency = intent.getIntExtra("intervalFrequency", LOCATION_INTERVAL_HIGH);
                Log.d("BroadCastReciever","recieved " + intervalFrequency);

                //stop old service
                stopService(new Intent(context, TripDataRecorder.class));

                //start new service
                startTripDataRecorder(intervalFrequency);
            }
        };

        registerReceiver(x, intentFilter);
    }

    private void startTripDataRecorder(int intervalFrequencey){
        Intent intent = new Intent(this, TripDataRecorder.class);
        intent.putExtra("intervalFrequency", intervalFrequencey);
        startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TripRecorder","service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("service_enabled", false);
        mEditor.apply();

        Log.d("TripRecorder", "service destroyed");

        if(mWakeLock != null){
            mWakeLock.release();
        }
        if(x != null) {
            unregisterReceiver(x);
        }

        stopService(new Intent(this, TripDataRecorder.class));
    }
}
