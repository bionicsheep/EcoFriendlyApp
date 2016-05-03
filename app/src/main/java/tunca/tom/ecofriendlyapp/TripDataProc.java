package tunca.tom.ecofriendlyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TripDataProc implements AsyncResponse {
    private static final int MAX_IDLE_TIME = 300; //5 minutes

    public static AsyncResponse delegate = null;

    private SQLiteDatabase mLocationDatabase;
    private SQLiteDatabase mTripDatabase;
    private SQLiteDatabase mSegmentDatabase;

    private double drivingTotal = 0;
    private double walkingTotal = 0;
    private double bikingTotal = 0;
    private double transitTotal = 0;

    private Context mContext;

    private String[] projection = {
            LocationHistoryDatabase.COL_1,
            LocationHistoryDatabase.COL_2,
            LocationHistoryDatabase.COL_3,
            LocationHistoryDatabase.COL_4,
            LocationHistoryDatabase.COL_5,
            LocationHistoryDatabase.COL_6
    };

    private String[] tripProjection = {
            TripDatabase.TRIP_COL_1,
            TripDatabase.TRIP_COL_2,
            TripDatabase.TRIP_COL_3,
            TripDatabase.TRIP_COL_4,
            TripDatabase.TRIP_COL_5,
    };

    private String[] segmentProjection = {
            SegmentHistoryDatabase.SEGMENT_COL_1,
            SegmentHistoryDatabase.SEGMENT_COL_2,
            SegmentHistoryDatabase.SEGMENT_COL_3,
            SegmentHistoryDatabase.SEGMENT_COL_4,
            SegmentHistoryDatabase.SEGMENT_COL_5,
            SegmentHistoryDatabase.SEGMENT_COL_6,
            SegmentHistoryDatabase.SEGMENT_COL_7,
            SegmentHistoryDatabase.SEGMENT_COL_8,
            SegmentHistoryDatabase.SEGMENT_COL_9
    };

    public TripDataProc(Context context){
        TripDataProc.delegate = this;
        initializeDatabase(context);
        mContext = context;
    }

    private void initializeDatabase(Context context){
        //location databse
        LocationHistoryDatabase mLocationDatabaseHelper = new LocationHistoryDatabase(context);
        mLocationDatabase = mLocationDatabaseHelper.getReadableDatabase();

        //processed trip database
        TripDatabase mTripDatabaseHelper = new TripDatabase(context);
        mTripDatabase = mTripDatabaseHelper.getReadableDatabase();

        //segment database
        SegmentHistoryDatabase mSegmentDatabaseHelper = new SegmentHistoryDatabase(context);
        mSegmentDatabase = mSegmentDatabaseHelper.getReadableDatabase();
    }

    public String[] loadHistory(String currentDate){
        String[] datesRecorded;
        String[] datesEntered;
        ArrayList<String> unenteredDatesList = new ArrayList<>();
        String[] unenteredDatesArr;

        String[] columns = {"DATE"};

        //query to get list
        Cursor c = mLocationDatabase.query(
                true, //unique values
                "location_data", //table
                columns, //only "date"
                null,
                null,
                null,
                null,
                null,
                null);

        Cursor d = mTripDatabase.query(
                true, //unique values
                "trip_data", //table
                columns, //only "date"
                null,
                null,
                null,
                null,
                null,
                null);

        datesRecorded = new String[c.getCount()];
        datesEntered = new String[d.getCount()];

        int row = c.getColumnIndex("DATE");
        int index = 0;

        //go through cursor of already recorded dates and exclude them
        for(d.moveToFirst(); !d.isAfterLast(); d.moveToNext()){
            datesEntered[index] = d.getString(row);
            index++;
        }

        index = 0;
        //go through cursor of returned values from query and add to array
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            datesRecorded[index] = c.getString(row);
            index++;
        }

        Log.d("TripDataProc","datesRecorded" + datesRecorded.length);

        //save days that haven't already been recorded (don't need to reprocess unless its today)
        for(String s : datesRecorded){
            outerloop:
            {
                for (String r: datesEntered) {
                    if (r.equals(s) && !s.equals(currentDate)) {
                        break outerloop;
                    }
                }
                unenteredDatesList.add(s); //date is unique or today
            }
        }
        c.close();
        d.close();

        unenteredDatesArr = new String[unenteredDatesList.size()];
        for(int x = 0; x < unenteredDatesArr.length; x++){
            unenteredDatesArr[x] = unenteredDatesList.get(x);
        }
        return unenteredDatesArr;
    }

    public void loadData(String date){
        ((MainActivity)(mContext)).history.clear();
        ((MainActivity)(mContext)).segments.clear();
        drivingTotal = 0;
        walkingTotal = 0;
        bikingTotal = 0;
        transitTotal = 0;

        Cursor c = mLocationDatabase.query(
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
                ((MainActivity)(mContext)).history.add(mEvent);
            }
        }

        c.close();
        findTrips(0);
        checkTrips(date);
    }

    private void findTrips(int startIndex){
        if(startIndex < ((MainActivity)(mContext)).history.size() - 1){
            Event start = findTripStart(startIndex);
            Event end = findTripEnd(start);

            TripSeg seg = new TripSeg(start,end, -1);
            ((MainActivity)(mContext)).segments.add(seg);

            findTrips(((MainActivity)(mContext)).history.indexOf(end) + 1);
        }

        Log.d("trips found","" + ((MainActivity)(mContext)).segments.size());
    }

    private Event findTripStart(int startIndex){
        Event start = ((MainActivity)(mContext)).history.get(startIndex);

        startIndex++;
        for(int x = startIndex; x < ((MainActivity)(mContext)).history.size() - 1; x++){
            TripSeg seg = new TripSeg(start, ((MainActivity)(mContext)).history.get(x), -1);
            if(seg.getDuration() < MAX_IDLE_TIME){
                return ((MainActivity)(mContext)).history.get(x);
            }
            start = ((MainActivity)(mContext)).history.get(x);
        }
        return ((MainActivity)(mContext)).history.get(((MainActivity)(mContext)).history.size() - 1);
    }

    private Event findTripEnd(Event start){
        int startIndex = ((MainActivity)(mContext)).history.indexOf(start);
        for(int x = startIndex; x < ((MainActivity)(mContext)).history.size() - 1; x++){
            TripSeg seg = new TripSeg(start, ((MainActivity)(mContext)).history.get(x), -1);
            if(seg.getDuration() > MAX_IDLE_TIME){
                return ((MainActivity)(mContext)).history.get(x);
            }
            start = ((MainActivity)(mContext)).history.get(x);
        }
        return ((MainActivity)(mContext)).history.get(((MainActivity)(mContext)).history.size() - 1);
    }

    private void checkTrips(String date){
        for(TripSeg s : ((MainActivity)(mContext)).segments){
            Log.d("TripDataProc","start: " + s.getStart());
            Log.d("TripDataProc","end: " + s.getEnd());
            Log.d("TripDataProc","date: " + date);
            Log.d("TripDataProc","segment index:" + ((MainActivity)(mContext)).segments.indexOf(s));
            getTimeEstimate(s.getStart(), s.getEnd(), ((MainActivity)(mContext)).segments.indexOf(s), date);
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
        Log.d("TripDataProc","id: " + id + " String.valueOf(id): " + String.valueOf(id));
        drvEst.execute(url1, url2, String.valueOf(id), date); //runs the estimate determination //coordinate 1, coordinate2, id
    }

    @Override
    public void onProcessFinish(String[] result) {
        Log.d("TripDataProc","loading outputs " + result.length);
        Log.d("TripDataProc","segments size " + ((MainActivity)(mContext)).segments.size());
        Log.d("TripDataProc","history size " + ((MainActivity)(mContext)).history.size());

        TripSeg seg = ((MainActivity)(mContext)).segments.get(Integer.parseInt(result[4]));

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

        addSegmentEntry(seg, lowest);

        //when done with all segments of day
        if(((MainActivity)(mContext)).segments.indexOf(seg) + 1 == ((MainActivity)(mContext)).segments.size()){
            Trip x = new Trip(result[5],drivingTotal,walkingTotal,bikingTotal,transitTotal); //create Trip (days date) and add to list
            ((MainActivity) mContext).loadNext();

            //add the TripData to the database
            addTripEntry(x);
        }
    }

    private void addTripEntry(Trip trip){
        ContentValues mValues = new ContentValues();

        mValues.put(TripDatabase.TRIP_COL_1, trip.getDate());
        mValues.put(TripDatabase.TRIP_COL_2, trip.getDriveDistance());
        mValues.put(TripDatabase.TRIP_COL_3, trip.getWalkDistance());
        mValues.put(TripDatabase.TRIP_COL_4, trip.getBikeDistance());
        mValues.put(TripDatabase.TRIP_COL_5, trip.getTransitDistance());

        mTripDatabase.insert(
                TripDatabase.TRIP_TABLE_NAME,
                null,
                mValues
        );
    }

    private void addSegmentEntry(TripSeg tripSeg, int mode){
        ContentValues mValues = new ContentValues();

        Event e1 = tripSeg.getStart();
        Event e2 = tripSeg.getEnd();

        Cursor c = mSegmentDatabase.query(
                SegmentHistoryDatabase.SEGMENT_TABLE_NAME,  // The table to query
                segmentProjection,                               // The columns to return
                null,
                null,
                null,
                null,
                null
        );

        int date1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_1);
        int time1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_2);
        int latitude1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_3);
        int longitude1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_4);
        int date2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_5);
        int time2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_6);
        int latitude2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_7);
        int longitude2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_8);

        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_1, e1.getDate());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_2, e1.getTime());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_3, e1.getLatitude());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_4, e1.getLongitude());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_5, e2.getDate());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_6, e2.getTime());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_7, e2.getLatitude());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_8, e2.getLongitude());
        mValues.put(SegmentHistoryDatabase.SEGMENT_COL_9, mode);

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            if(e1.getDate().equals(c.getString(date1)) &&
                    e1.getTime().equals(c.getString(time1)) &&
                    e1.getLatitude() == (double)c.getDouble(latitude1) &&
                    e1.getLongitude() == (double)c.getDouble(longitude1) &&
                    e2.getDate().equals(c.getString(date2)) &&
                    e2.getTime().equals(c.getString(time2)) &&
                    e2.getLatitude() == (double)c.getDouble(latitude2) &&
                    e2.getLongitude() == (double)c.getDouble(longitude2))
            { //basically if its already in table, quit
                c.close();
                return;
            }
        }
        //else add it
        mSegmentDatabase.insert(
                SegmentHistoryDatabase.SEGMENT_TABLE_NAME,
                null,
                mValues
        );

        c.close();


    }

    //get all the processed trips
    public ArrayList<Trip> getProcessedTripsList(){
        ArrayList<Trip> processedTripsList = new ArrayList<>();

        Cursor c = mTripDatabase.query(
                TripDatabase.TRIP_TABLE_NAME,  // The table to query
                tripProjection,                               // The columns to return
                null,
                null,
                null,
                null,
                null
        );

        int dateRow = c.getColumnIndex(TripDatabase.TRIP_COL_1);
        int driveRow = c.getColumnIndex(TripDatabase.TRIP_COL_2);
        int walkRow = c.getColumnIndex(TripDatabase.TRIP_COL_3);
        int bikeRow = c.getColumnIndex(TripDatabase.TRIP_COL_4);
        int transitRow = c.getColumnIndex(TripDatabase.TRIP_COL_5);

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            Trip t = new Trip (c.getString(dateRow),c.getDouble(driveRow),c.getDouble(walkRow),c.getDouble(bikeRow),c.getDouble(transitRow));
            processedTripsList.add(t);
        }

        c.close();
        return processedTripsList;
    }

    public ArrayList<TripSeg> getSegments(String date){
        ArrayList<TripSeg> tripSegs = new ArrayList<>();

        Cursor c = mSegmentDatabase.query(
                SegmentHistoryDatabase.SEGMENT_TABLE_NAME,  // The table to query
                segmentProjection,                               // The columns to return
                null,
                null,
                null,
                null,
                null
        );

        int date1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_1);
        int time1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_2);
        int latitude1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_3);
        int longitude1 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_4);
        int date2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_5);
        int time2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_6);
        int latitude2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_7);
        int longitude2 = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_8);
        int modeTrans = c.getColumnIndex(SegmentHistoryDatabase.SEGMENT_COL_9);

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            if(c.getString(date1).equals(date)){
                Event e1 = new Event(c.getString(date1), c.getString(time1), c.getDouble(latitude1), c.getDouble(longitude1), 0, 0);
                Event e2 = new Event(c.getString(date2), c.getString(time2), c.getDouble(latitude2), c.getDouble(longitude2), 0, 0);
                int mode = c.getInt(modeTrans);
                TripSeg s = new TripSeg(e1, e2, mode);
                tripSegs.add(s);
            }
        }
        return tripSegs;
    }

    //===============================================
    // based off of Android sdk documentation example
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
                    //give time if google times out and try again after a couple of seconds
                    {
                        while (true) {
                            Log.d("TripDataProc","getting estimate for trip" + params[2]);
                            Log.d("sleepTimer","" + sleepTimer);
                            try {
                                Thread.sleep(sleepTimer);
                            } catch (InterruptedException ex) {
                                //do nothing
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
                }

                for(int x = 0; x < 4; x++){
                    outputs[x] = getEstimate(outputs[x]);
                }

                Log.d("TripDataProc","params 2 id: " + params[2]);
                outputs[4] = params[2]; //id
                outputs[5] = params[3]; //date
                Log.d("TripDataProc","done loading outputs " + outputs.length);
            } catch (IOException e) {
                Log.d("TripDatProc","MOTHERFUCKINGIOEXCEPTION");
                e.printStackTrace();
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
                //do nothing
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
