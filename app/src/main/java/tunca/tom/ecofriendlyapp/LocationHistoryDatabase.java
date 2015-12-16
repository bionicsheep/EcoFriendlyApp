package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class LocationHistoryDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "LocationData.db";
    public static final String TABLE_NAME = "location_data";
    public static final String COL_1 = "DATE";
    public static final String COL_2 = "TIME";
    public static final String COL_3 = "X_COOR";
    public static final String COL_4 = "Y_COOR";
    public static final String COL_5 = "VELOCITY";
    public static final String COL_6 = "ACCURACY";

    private static final String SQL_INIT_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_1 + " TEXT," +
                    COL_2 + " TEXT," +
                    COL_3 + " REAL," +
                    COL_4 + " REAL," +
                    COL_5 + " REAL," +
                    COL_6 + " REAL)";

    public LocationHistoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_INIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_INIT_TABLE);
    }
}
