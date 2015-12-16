package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;


public class TripDataProcessor {

    private SQLiteDatabase mDatabase;
    private LocationHistoryDatabase mDatabaseHelper;

    private ArrayList<Event> history = new ArrayList<Event>();
    private ArrayList<TripSegment> segments = new ArrayList<TripSegment>();

    private String[] projection = {
            LocationHistoryDatabase.COL_1,
            LocationHistoryDatabase.COL_2,
            LocationHistoryDatabase.COL_3,
            LocationHistoryDatabase.COL_4,
            LocationHistoryDatabase.COL_5,
            LocationHistoryDatabase.COL_6
    };


    public TripDataProcessor(Context context){
        initializeDatabase(context);
    }

    private void initializeDatabase(Context context){
        mDatabaseHelper = new LocationHistoryDatabase(context);
        mDatabase = mDatabaseHelper.getReadableDatabase();
    }

    public void loadData(String date){

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
        int xRow = c.getColumnIndex(LocationHistoryDatabase.COL_3);
        int yRow = c.getColumnIndex(LocationHistoryDatabase.COL_4);
        int velocity = c.getColumnIndex(LocationHistoryDatabase.COL_5);
        int accuracy = c.getColumnIndex(LocationHistoryDatabase.COL_6);

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            Log.d("date","" + date);
            Log.d("daterow","" + c.getString(dateRow));
            if(date.equals(c.getString(dateRow))) {
                Event mEvent = new Event(c.getString(dateRow), c.getString(timeRow),
                        c.getDouble(xRow), c.getDouble(yRow), c.getDouble(velocity),
                        c.getDouble(accuracy));
                history.add(mEvent);
            }
        }

        processData(0);
    }

    public void processData(int start){
        int driveProof = 0;
        int walkProof = 0;
        int startPoint = start;

        for(int x = startPoint; x + 1 < history.size(); x++){
            double disDif = distanceDifference(history.get(x+1), history.get(x));
            double timeDif = getTimeDifference(history.get(x+1).getTime(), history.get(x).getTime());
            Log.d("div","" + (disDif/timeDif));
            if((disDif/timeDif) > 3){
                driveProof++;
            }else if((disDif/timeDif) < 3){
                walkProof++;
            }

            if(walkProof > 3){
                processData(findWalkEnd(startPoint));
            }else if(driveProof > 3){
                processData(findDriveEnd(startPoint));
            }
        }
    }

    private int findWalkEnd(int start){
        int walkCancel = 0;
        for(int x = start; x + 1 < history.size(); x++){
            double disDif = distanceDifference(history.get(x+1), history.get(x));
            double timeDif = getTimeDifference(history.get(x+1).getTime(), history.get(x).getTime());
            if((disDif/timeDif) < 3){
                walkCancel++;
            }else if((disDif/timeDif) > 3){
                walkCancel = 0;
            }

            if(walkCancel > 3){
                TripSegment segment = new TripSegment(start,x,1);
                segments.add(segment);
                return x;
            }
        }

        return history.size();
    }

    private int findDriveEnd(int start){
        int driveCancel = 0;
        for(int x = start; x + 1 < history.size(); x++){
            double disDif = distanceDifference(history.get(x+1), history.get(x));
            double timeDif = getTimeDifference(history.get(x+1).getTime(), history.get(x).getTime());
            if((disDif/timeDif) > 3){
                driveCancel++;
            }else if((disDif/timeDif) < 3){
                driveCancel = 0;
            }

            if(driveCancel > 3){
                TripSegment segment = new TripSegment(start,x,0);
                segments.add(segment);
                return x;
            }
        }
        return history.size();
    }

    private double distanceDifference(Event event1, Event event2) {
        float[] results = new float[1];
        Location.distanceBetween(event1.getxCoor(), event1.getyCoor(),
                event2.getxCoor(), event2.getyCoor(), results);

        return (double)results[0];
    }

    private int getTimeDifference(String time2, String time1) {
        int hour1 = Integer.parseInt(time1.substring(0, 1));
        int minute1 = Integer.parseInt(time1.substring(2, 3));
        int second1 = Integer.parseInt(time1.substring(4, 5));

        int hour2 = Integer.parseInt(time2.substring(0, 1));
        int minute2 = Integer.parseInt(time2.substring(2, 3));
        int second2 = Integer.parseInt(time2.substring(4, 5));

        int totTime1 = (hour1 * 60 * 60) + (minute1 * 60) + second1;
        int totTime2 = (hour2 * 60 * 60) + (minute2 * 60) + second2;

        return totTime2 - totTime1;

    }


    public double getDriveDistance(){
        double driveDistance = 0;

        for(int x = 0; x < segments.size(); x++){
            TripSegment seg = segments.get(x);

            if(seg.type == 0){
                for(int n = seg.getStart(); n < seg.getEnd() - 1; n++){
                    double disDif = distanceDifference(history.get(n+1), history.get(n));
                    driveDistance += disDif;
                }
            }
        }

        return driveDistance;
    }

    public double getWalkDistance(){
        double walkDistance = 0;

        for(int x = 0; x < segments.size(); x++){
            TripSegment seg = segments.get(x);

            if(seg.type == 1){
                for(int n = seg.getStart(); n < seg.getEnd() - 1; n++){
                    double disDif = distanceDifference(history.get(n+1), history.get(n));
                    walkDistance += disDif;
                }
            }
        }

        return walkDistance;
    }
}

class TripSegment{
    int start;
    int end;
    int type;

    public TripSegment(int start, int end, int type){
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getStart(){
        return start;
    }

    public int getEnd(){
        return end;
    }

    public int getType(){
        return type;
    }
}
