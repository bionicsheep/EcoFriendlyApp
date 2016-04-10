package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TripDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String TRIP_DATABASE_NAME = "TripData.db";
    public static final String TRIP_TABLE_NAME = "trip_data";
    public static final String TRIP_COL_1 = "DATE";
    public static final String TRIP_COL_2 = "DRIVE";
    public static final String TRIP_COL_3 = "WALK";
    public static final String TRIP_COL_4 = "BIKE";
    public static final String TRIP_COL_5 = "TRANSIT";

    private static final String SQL_INIT_TABLE =
            "CREATE TABLE " + TRIP_TABLE_NAME + " (" +
                    TRIP_COL_1 + " TEXT," +
                    TRIP_COL_2 + " REAL," +
                    TRIP_COL_3 + " REAL," +
                    TRIP_COL_4 + " REAL," +
                    TRIP_COL_5 + " REAL)";

    public TripDatabase(Context context) {
        super(context, TRIP_DATABASE_NAME, null, DATABASE_VERSION);
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
