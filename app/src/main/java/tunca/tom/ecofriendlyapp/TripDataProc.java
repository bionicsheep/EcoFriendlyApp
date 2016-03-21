package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TripDataProc implements AsyncResponse {

    private static final int MIN_UNIQUE_DISTANCE = 10;
    private static final int MAX_NON_UNIQUE_EVENTS = 15;
    private static final int MIN_UNIQUE_EVENTS = 2;

    public static AsyncResponse delegate = null;

    private SQLiteDatabase mDatabase;
    private LocationHistoryDatabase mDatabaseHelper;

    private ArrayList<Event> history = new ArrayList<>();
    private ArrayList<TripSeg> segments = new ArrayList<>();

    private int bikingTotal = 0;
    private int drivingTotal = 0;
    private int walkingTotal = 0;
    private int transitTotal = 0;

    private int daysRecorded = 0;

    private ArrayList<Trip> mTripList = new ArrayList<>();

    private Context c;

    private String[] projection = {
            LocationHistoryDatabase.COL_1,
            LocationHistoryDatabase.COL_2,
            LocationHistoryDatabase.COL_3,
            LocationHistoryDatabase.COL_4,
            LocationHistoryDatabase.COL_5,
            LocationHistoryDatabase.COL_6
    };

    public TripDataProc(Context context){
        TripDataProc.delegate = this;
        initializeDatabase(context);
        c = context;
    }

    private void initializeDatabase(Context context){
        mDatabaseHelper = new LocationHistoryDatabase(context);
        mDatabase = mDatabaseHelper.getReadableDatabase();
    }

    public ArrayList<Trip> loadHistory(){
        String[] dates;

        String[] columns = {"DATE"};
        //query to get list
        Cursor c = mDatabase.query(
                true, //unique values
                "location_data", //table
                columns, //only "date"
                null,
                null,
                null,
                null,
                null,
                null);

        daysRecorded = c.getCount(); //number of days we have recorded during

        dates = new String[c.getCount()];
        int row = c.getColumnIndex("DATE");
        int index = 0;

        //go through cursor of returned values from query and add to array
        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            dates[index] = c.getString(row);
            index++;
        }
        c.close();

        for(String x : dates){
            loadData(x);
        }

        return mTripList;
    }

    public void loadData(String date){
        history.clear();
        segments.clear();
        bikingTotal = 0;
        drivingTotal = 0;
        walkingTotal = 0;
        transitTotal = 0;

        Cursor c = mDatabase.query(
                LocationHistoryDatabase.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        int dateRow = c.getColumnIndex(LocationHistoryDatabase.COL_1);
        int timeRow = c.getColumnIndex(LocationHistoryDatabase.COL_2);
        int latitude = c.getColumnIndex(LocationHistoryDatabase.COL_3);
        int longitude = c.getColumnIndex(LocationHistoryDatabase.COL_4);
        int velocity = c.getColumnIndex(LocationHistoryDatabase.COL_5);
        int accuracy = c.getColumnIndex(LocationHistoryDatabase.COL_6);

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            if(date.equals(c.getString(dateRow))) {
                Event mEvent = new Event(c.getString(dateRow), c.getString(timeRow),
                        c.getDouble(latitude), c.getDouble(longitude), c.getDouble(velocity),
                        c.getDouble(accuracy));
                history.add(mEvent);
            }
        }

        findTrips(0);
        checkTrips(date);
    }

    private void findTrips(int startIndex){
        if(startIndex < history.size() - 1){
            Event start = history.get(startIndex);
            start = findTripStart(start);
            Event end = findTripEnd(start);

            TripSeg seg = new TripSeg(start,end);
            segments.add(seg);

            findTrips(history.indexOf(end));
        }
    }


    private Event findTripStart(Event start){
        int uniqueEvents = 0;
        Event temp = start;
        int startIndex = history.indexOf(start);

        for(int x = startIndex; x < history.size() - 1; x++){
            if(distanceDifference(history.get(x+1), history.get(x)) > MIN_UNIQUE_DISTANCE){
                if(uniqueEvents == 0){
                    temp = history.get(x);
                }
                uniqueEvents++;
            }else{
                uniqueEvents = 0;
            }
            if(uniqueEvents > MIN_UNIQUE_EVENTS){
                return temp;
            }
        }
        return start;
    }


    private Event findTripEnd(Event start){
        Event temp = start;
        int nonUniqueEvents = 0;
        int startIndex = history.indexOf(start) + 1;

        for(int x = startIndex; x < history.size() - 1; x++){

            if(distanceDifference(history.get(x+1), history.get(x)) < MIN_UNIQUE_DISTANCE){
                if(nonUniqueEvents == 0){
                    temp = history.get(x);
                }
                nonUniqueEvents++;
            }else{
                nonUniqueEvents = 0;
            }
            if(nonUniqueEvents > MAX_NON_UNIQUE_EVENTS){
                return temp;
            }
        }
        return history.get(history.size() - 1);
    }

    private void checkTrips(String date){
        for(TripSeg s : segments){
            getTimeEstimate(s.getStart(), s.getEnd(), segments.indexOf(s), date);
        }
    }

    private void getTimeEstimate(Event start, Event end, int id, String date){
        double lat1 = start.getLatitude();
        double long1 = start.getLongitude();
        double lat2 = end.getLatitude();
        double long2 = end.getLongitude();

        String url1 = lat1 + "," + long1;
        String url2 = lat2 + "," + long2;

        TimeEstimate drvEst = new TimeEstimate(); //async task object
        drvEst.execute(url1, url2, String.valueOf(id), date); //runs the estimate determination //coordinate 1, coordinate2, id
    }

    private double distanceDifference(Event event1, Event event2) {
        float[] results = new float[1];
        Location.distanceBetween(event1.getLatitude(), event1.getLongitude(),
                event2.getLatitude(), event2.getLongitude(), results);

        return (double)results[0];
    }

    @Override
    public void onProcessFinish(String[] result) {
        TripSeg seg = segments.get(Integer.parseInt(result[4]));

        int actualTimeDif = seg.getDuration();
        int dif = Math.abs(actualTimeDif - Integer.parseInt(result[0]));
        int lowest = 0;

        for(int x = 1; x < result.length; x++){
            Log.d("size","" + x + " " + result.length);
            int tempDiff = Math.abs(actualTimeDif - Integer.parseInt(result[x]));
            if(tempDiff < dif){
                dif = tempDiff;
                lowest = x;
            }
        }

        if(lowest == 0){
            drivingTotal += seg.distanceDifference();
        }
        else if(lowest == 1){
            walkingTotal += seg.distanceDifference();
        }
        else if(lowest == 2){
            bikingTotal += seg.distanceDifference();
        }
        else if(lowest == 3){
            transitTotal += seg.distanceDifference();
        }

        //when done with all segments of day
        if(segments.indexOf(seg) + 1 == segments.size()){
            Trip x = new Trip(result[5],walkingTotal,drivingTotal,transitTotal,bikingTotal); //create Trip (days date) and add to list
            mTripList.add(x);

            if(mTripList.size() == daysRecorded){
                //all the async tasks have finished and the arraylist is processed
                ((MainActivity)c).loadCompleteLists(mTripList);
            }
        }
    }


    //===============================================
    //took from google.com
    private class TimeEstimate extends AsyncTask<String, String, String[]>{
        @Override
        protected String[] doInBackground(String... params) {
            String[] outputs = new String[6];
            try {
                String url0 = "http://maps.googleapis.com/maps/api/directions/json?origin=" + params[0] + "&destination=" + params[1] + "&mode=" + "driving" + "&sensor=false";
                String url1 = "http://maps.googleapis.com/maps/api/directions/json?origin=" + params[0] + "&destination=" + params[1] + "&mode=" + "walking" + "&sensor=false";
                String url2 = "http://maps.googleapis.com/maps/api/directions/json?origin=" + params[0] + "&destination=" + params[1] + "&mode=" + "bicycling" + "&sensor=false";
                String url3 = "http://maps.googleapis.com/maps/api/directions/json?origin=" + params[0] + "&destination=" + params[1] + "&mode=" + "transit" + "&sensor=false";

                String[] urls = {url0,url1,url2,url3};

                for(int x = 0; x < 4; x++) {
                    int sleepTimer = 0;

                    getStream:
                    while (true) {
                        Log.d("sleepTimer","" + sleepTimer);
                        try {
                            Thread.sleep(sleepTimer);
                        } catch (InterruptedException ex) {

                        }
                        sleepTimer += 500;

                        HttpPost httppost = new HttpPost(urls[x]);
                        HttpClient client = new DefaultHttpClient();
                        HttpResponse response = client.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        InputStream stream = entity.getContent();
                        outputs[x] = getStreamOutput(stream);
                        if(!isEmpty(outputs[x])){
                            break getStream;
                        }
                    }
                }

                for(int x = 0; x < 4; x++){
                    outputs[x] = getEstimate(outputs[x]);
                }

                outputs[4] = params[2]; //id
                outputs[5] = params[3]; //date

            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            return outputs;

        }

        private String getStreamOutput(InputStream stream){
            StringBuilder stringBuilder = new StringBuilder();
            try {
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            }
            catch (IOException e) {
            }

            return stringBuilder.toString();
        }

        private String getEstimate(String streamOutput){
            String estimate = "";
            try {
                JSONObject jsonObject = new JSONObject(streamOutput);
                JSONArray array = jsonObject.getJSONArray("routes");
                JSONObject routes = array.getJSONObject(0);
                JSONArray legs = routes.getJSONArray("legs");
                JSONObject steps = legs.getJSONObject(0);
                JSONObject duration = steps.getJSONObject("duration");

                estimate = (duration.getString("value"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return estimate;
        }

        private boolean isEmpty(String streamOutput){
            try {
                JSONObject jsonObject = new JSONObject(streamOutput);
                if(jsonObject.has("error_message")){
                    return true;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            delegate.onProcessFinish(result);
        }
    }
}
