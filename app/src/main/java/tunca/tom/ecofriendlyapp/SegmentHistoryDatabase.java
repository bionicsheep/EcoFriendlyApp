package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SegmentHistoryDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String SEGMENT_DATABASE_NAME = "SegmentData.db";
    public static final String SEGMENT_TABLE_NAME = "segment_data";
    public static final String SEGMENT_COL_1 = "DATE1";
    public static final String SEGMENT_COL_2 = "TIME1";
    public static final String SEGMENT_COL_3 = "LAT1";
    public static final String SEGMENT_COL_4 = "LONG1";
    public static final String SEGMENT_COL_5 = "DATE2";
    public static final String SEGMENT_COL_6 = "TIME2";
    public static final String SEGMENT_COL_7 = "LAT2";
    public static final String SEGMENT_COL_8 = "LONG2";
    public static final String SEGMENT_COL_9 = "MODE";

    private static final String SQL_INIT_TABLE =
            "CREATE TABLE " + SEGMENT_TABLE_NAME + " (" +
                    SEGMENT_COL_1 + " TEXT," +
                    SEGMENT_COL_2 + " TEXT," +
                    SEGMENT_COL_3 + " REAL," +
                    SEGMENT_COL_4 + " REAL," +
                    SEGMENT_COL_5 + " TEXT," +
                    SEGMENT_COL_6 + " TEXT," +
                    SEGMENT_COL_7 + " REAL," +
                    SEGMENT_COL_8 + " REAL," +
                    SEGMENT_COL_9 + " REAL)";

    public SegmentHistoryDatabase(Context context) {
        super(context, SEGMENT_DATABASE_NAME, null, DATABASE_VERSION);
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

