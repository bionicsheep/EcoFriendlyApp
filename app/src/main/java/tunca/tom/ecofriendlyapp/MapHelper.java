package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapHelper {

    private GoogleMap mMap;
    private SQLiteDatabase mDatabase;
    private LocationHistoryDatabase mDatabaseHelper;

    private String[] projection = {
            LocationHistoryDatabase.COL_1,
            LocationHistoryDatabase.COL_3,
            LocationHistoryDatabase.COL_4
        };


    public MapHelper(Context context){
        initializeDatabase(context);
    }

    private void initializeDatabase(Context context){
        mDatabaseHelper = new LocationHistoryDatabase(context);
        mDatabase = mDatabaseHelper.getReadableDatabase();
    }

    public void setGoogleMap(GoogleMap map){
        mMap = map;
    }

    public void paintHistory(String date) {
        if(mMap == null){
            return;
        }

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
        int xRow = c.getColumnIndex(LocationHistoryDatabase.COL_3);
        int yRow = c.getColumnIndex(LocationHistoryDatabase.COL_4);

        ArrayList<Marker> markers = new ArrayList<Marker>();
        LatLng loc = new LatLng(0, 0);

        if(c.getCount() == 0){
            return;
        }

        c.moveToFirst();
        double tempX = 0;
        double tempY =  0;

        Log.d("weee"," " + c.getCount());


        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            Log.d("date", "" + c.getString(dateRow).equals(date));

            if(c.getString(dateRow).equals(date)) {
                Log.d("MapHelper","distance difference " + distanceDifference(tempX, tempY, c.getFloat(xRow), c.getFloat(yRow)));
                loc = new LatLng(c.getFloat(xRow), c.getFloat(yRow));
                Log.d("test", "iterate");

                Marker m = mMap.addMarker(new MarkerOptions()
                        .title("")
                        .snippet("")
                        .position(loc));

                markers.add(m);
                tempX = c.getFloat(xRow);
                tempY = c.getFloat(xRow);
            }
        }


        LatLngBounds.Builder b = new LatLngBounds.Builder();

        for(Marker x: markers){
            b.include(x.getPosition());
        }
        
        try{
            LatLngBounds bounds = b.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10,10,10);
            mMap.animateCamera(cu);
        }catch(IllegalStateException ex) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));
        }

    }

    public double distanceDifference(double xCoor1, double yCoor1, double xCoor2, double yCoor2) {
        double difference = Math.sqrt((xCoor1-xCoor2)*(xCoor1-xCoor2) + (yCoor1-yCoor2)*(yCoor1-yCoor2));

        difference = (difference / 111319.0);

        return difference;
    }

}
