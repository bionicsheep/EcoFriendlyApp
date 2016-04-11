package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

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

    private String[] projection = {
            LocationHistoryDatabase.COL_1,
            LocationHistoryDatabase.COL_3,
            LocationHistoryDatabase.COL_4
        };


    public MapHelper(Context context){
        initializeDatabase(context);
    }

    private void initializeDatabase(Context context){
        LocationHistoryDatabase mDatabaseHelper = new LocationHistoryDatabase(context);
        mDatabase = mDatabaseHelper.getReadableDatabase();
    }

    public void setGoogleMap(GoogleMap map){
        mMap = map;
    }

    public void paintHistory(String date) {
        if(mMap == null){
            return;
        }

        mMap.clear();

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

        ArrayList<Marker> markers = new ArrayList<>();
        LatLng loc = new LatLng(38.9072,-77.0369);

        if(c.getCount() == 0){
            return;
        }

        c.moveToFirst();
        double tempX = 0;
        double tempY =  0;

        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            if(c.getString(dateRow).equals(date)) {
                double dif = distanceDifference(tempX, tempY, c.getFloat(xRow), c.getFloat(yRow));
                loc = new LatLng(c.getFloat(xRow), c.getFloat(yRow));

                if(dif > 15) {
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .title("")
                            .snippet("")
                            .position(loc));

                    markers.add(m);
                    tempX = c.getFloat(xRow);
                    tempY = c.getFloat(yRow);
                }
            }
        }

        c.close();

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
        float[] results = new float[2];
        Location.distanceBetween(yCoor1, xCoor1, yCoor2, xCoor2, results);
        return results[0];
    }

}
