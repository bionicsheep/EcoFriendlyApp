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

    private void test(){
        addEntry(new Event("03222016","074820",38.9029262,-77.0497204,1.00895476341248,6.0));
        addEntry(new Event("03222016","074842",38.9030914,-77.0498804,1.10279619693756,8.0));
        addEntry(new Event("03222016","074853",38.9031208,-77.050053,0.735344707965851,12.0));
        addEntry(new Event("03222016","074904",38.9031447,-77.0502229,0.991798222064972,16.0));
        addEntry(new Event("03222016","074914",38.9031248,-77.0503219,0.948775112628937,12.0));
        addEntry(new Event("03222016","074925",38.9030622,-77.0504607,1.19494271278381,12.0));
        addEntry(new Event("03222016","074936",38.9029617,-77.0506248,1.1835321187973,8.0));
        addEntry(new Event("03222016","074946",38.9029329,-77.0507582,1.18348252773285,8.0));
        addEntry(new Event("03222016","074957",38.9030415,-77.0509591,1.41365730762482,8.0));
        addEntry(new Event("03222016","075007",38.9030996,-77.0511414,0.00012471858644858,16.0));
        addEntry(new Event("03222016","075018",38.903142,-77.0512934,0.921469151973724,12.0));
        addEntry(new Event("03222016","075028",38.9032228,-77.0515696,1.06208896636963,16.0));
        addEntry(new Event("03222016","075129",38.9035486,-77.052295,1.06719040870667,8.0));
        addEntry(new Event("03222016","075140",38.9035697,-77.0525024,1.12340092658997,8.0));
        addEntry(new Event("03222016","075151",38.9035907,-77.0527189,1.05493938922882,16.0));
        addEntry(new Event("03222016","075202",38.9036109,-77.0529139,1.03905200958252,24.0));
        addEntry(new Event("03222016","075213",38.9037357,-77.0531573,1.22987484931946,24.0));
        addEntry(new Event("03222016","075223",38.903809,-77.0532871,1.22766816616058,12.0));
        addEntry(new Event("03222016","075324",38.9041425,-77.0541148,1.24837934970856,8.0));
        addEntry(new Event("03222016","075334",38.9041752,-77.0542805,1.21531748771667,8.0));
        addEntry(new Event("03222016","075345",38.9042376,-77.0545378,1.27949392795563,24.0));
        addEntry(new Event("03222016","075355",38.9043217,-77.0545756,0.761651158332825,16.0));
        addEntry(new Event("03222016","075406",38.9043693,-77.0547823,1.37609314918518,24.0));
        addEntry(new Event("03222016","075416",38.9044515,-77.0549734,0.695824861526489,16.0));
        addEntry(new Event("03222016","075427",38.9044768,-77.055154,1.15778183937073,16.0));
        addEntry(new Event("03222016","075525",38.9046746,-77.0559741,1.09997272491455,16.0));
        addEntry(new Event("03222016","075536",38.9047274,-77.0561213,0.9127476811409,16.0));
        addEntry(new Event("03222016","075546",38.9047747,-77.0562969,1.1975634098053,8.0));
        addEntry(new Event("03222016","075557",38.9048815,-77.05647,1.21784722805023,12.0));
        addEntry(new Event("03222016","075608",38.9049454,-77.056698,1.11201822757721,16.0));
        addEntry(new Event("03222016","075618",38.9049984,-77.0568661,1.07730102539063,12.0));
        addEntry(new Event("03222016","075629",38.9049921,-77.0570299,1.06756889820099,12.0));
        addEntry(new Event("03222016","075640",38.9049,-77.0571513,0.702209770679474,16.0));
        addEntry(new Event("03222016","075650",38.9049018,-77.0573498,0.752362012863159,16.0));
        addEntry(new Event("03222016","075745",38.9051382,-77.058057,1.15029561519623,24.0));
        addEntry(new Event("03222016","075755",38.9051924,-77.0582235,1.0648467540741,16.0));
        addEntry(new Event("03222016","075805",38.9053061,-77.0583796,0.775079846382141,16.0));
        addEntry(new Event("03222016","075815",38.9053325,-77.058452,0.623761236667633,12.0));
        addEntry(new Event("03222016","075825",38.905425,-77.0585961,0.547074675559998,16.0));
        addEntry(new Event("03222016","075836",38.905468,-77.0586878,0.73759937286377,16.0));
        addEntry(new Event("03222016","075847",38.9055178,-77.0588397,0.601972639560699,24.0));
        addEntry(new Event("03222016","075857",38.9055139,-77.059028,0.00241878884844482,12.0));
        addEntry(new Event("03222016","075908",38.9055264,-77.0590408,0.356522798538208,12.0));
        addEntry(new Event("03222016","075918",38.9054798,-77.0593697,1.00087773799896,16.0));
        addEntry(new Event("03222016","075929",38.9053698,-77.0594494,1.14559650421143,12.0));
        addEntry(new Event("03222016","075939",38.9053097,-77.059564,1.16666984558105,12.0));
        addEntry(new Event("03222016","075950",38.905264,-77.0597435,0.725259482860565,16.0));
        addEntry(new Event("03222016","080000",38.9051907,-77.059883,0.960815787315369,16.0));
        addEntry(new Event("03222016","080053",38.9052006,-77.0606346,0.36271071434021,16.0));
        addEntry(new Event("03222016","080104",38.9051677,-77.0606733,0.467020153999329,12.0));
        addEntry(new Event("03222016","080114",38.9052549,-77.0609812,0.416769146919251,12.0));
        addEntry(new Event("03222016","080125",38.9053061,-77.0610799,0.0,16.0));
        addEntry(new Event("03222016","080147",38.9052536,-77.0613053,0.668533742427826,16.0));
        addEntry(new Event("03222016","080157",38.9053248,-77.0613767,0.273920029401779,12.0));
        addEntry(new Event("03222016","080208",38.9052977,-77.0617387,1.0924516916275,24.0));
        addEntry(new Event("03222016","080218",38.905307,-77.0617775,0.533584535121918,12.0));
        addEntry(new Event("03222016","080229",38.9052927,-77.0621154,0.678334951400757,16.0));
        addEntry(new Event("03222016","080239",38.9052786,-77.0622702,0.417998343706131,16.0));
        addEntry(new Event("03222016","080249",38.9052857,-77.0624913,0.89342612028122,16.0));
        addEntry(new Event("03222016","080259",38.9052904,-77.0626369,0.404071688652039,12.0));
        addEntry(new Event("03222016","080331",38.9055477,-77.0627088,0.50459361076355,16.0));
        addEntry(new Event("03222016","080342",38.9056392,-77.0627343,0.471507012844086,16.0));
        addEntry(new Event("03222016","080451",38.9065499,-77.0630748,1.06867384910584,8.0));
        addEntry(new Event("03222016","080501",38.9065748,-77.0630798,0.612066030502319,12.0));
        addEntry(new Event("03222016","080521",38.9068334,-77.0629499,0.0,6.0));
        addEntry(new Event("03222016","080531",38.9068812,-77.0629875,0.432152271270752,12.0));
        addEntry(new Event("03222016","080541",38.9070049,-77.0630323,0.433015555143356,16.0));
        addEntry(new Event("03222016","080552",38.9072059,-77.063202,0.861065208911896,16.0));
        addEntry(new Event("03222016","081603",38.9065689,-77.0629108,4.6169605255127,24.0));
        addEntry(new Event("03222016","082012",38.9051207,-77.0603965,0.0,12.0));
        addEntry(new Event("03222016","082126",38.9051101,-77.0577216,0.0,16.0));
        addEntry(new Event("03222016","082147",38.905008,-77.0574881,2.19145488739014,16.0));
        addEntry(new Event("03222016","082414",38.9034754,-77.0531438,8.94217586517334,8.0));
        addEntry(new Event("03222016","082516",38.9030754,-77.0520015,0.0,8.0));
        addEntry(new Event("03222016","082526",38.9029514,-77.0517228,2.24912667274475,12.0));
        addEntry(new Event("03222016","082712",38.9025466,-77.0508546,3.84016799926758,8.0));
        addEntry(new Event("03222016","082805",38.9021415,-77.0490915,1.31485223770142,8.0));
        addEntry(new Event("03222016","082815",38.9022327,-77.0491976,1.1889773607254,6.0));
        addEntry(new Event("03222016","082826",38.902375,-77.0493127,1.25357747077942,6.0));
        addEntry(new Event("03222016","082837",38.9025388,-77.0493369,1.15370750427246,8.0));
        addEntry(new Event("03222016","082909",38.9027666,-77.0493279,0.578335762023926,16.0));
        addEntry(new Event("03222016","082931",38.9028952,-77.0498351,0.0,24.5310001373291));
        addEntry(new Event("03232016","005758",38.9065948,-77.0470466,0.0,20.6499996185303));
        addEntry(new Event("03232016","005808",38.9067249,-77.0467767,0.0,20.2590007781982));
        addEntry(new Event("03232016","005818",38.9067499,-77.0466418,0.0,20.2159996032715));
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
            if ((distanceDifferenceX(minX, maxX) + distanceDifferenceY(minY, maxY)) > MIN_TOTAL_CHANGE || true) {
                //make sure outlier isn't the cause
                Log.d("diff of centers","" + (distanceDifferenceX(centerX, weightedCenterX) +
                        distanceDifferenceY(centerY, weightedCenterY)));
                Log.d("must be less than","" + ALLOW_CENTER_DIFFERENCE);
                if (((distanceDifferenceX(centerX, weightedCenterX) + distanceDifferenceY(centerY, weightedCenterY)) < ALLOW_CENTER_DIFFERENCE) || true) {
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