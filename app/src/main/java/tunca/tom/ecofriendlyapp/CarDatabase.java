package tunca.tom.ecofriendlyapp;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;

    public static final String CAR_DATABASE_NAME = "CarData.db";
    public static final String CAR_TABLE_NAME = "vehicles";

    private static final String SQL_INIT_TABLE =
            "CREATE TABLE " + CAR_TABLE_NAME + " (" +
                    "barrels08 NUMERIC(19, 17)," +
                    "barrelsA08 NUMERIC(19, 17)," +
                    "charge120 NUMERIC(2, 1)," +
                    "charge240 NUMERIC(4, 2)," +
                    "city08 INT," +
                    "city08U NUMERIC(7, 4)," +
                    "cityA08 INT," +
                    "cityA08U NUMERIC(7, 4)," +
                    "cityCD NUMERIC(5, 4)," +
                    "cityE NUMERIC(7, 4)," +
                    "cityUF NUMERIC(5, 4)," +
                    "co2 INT," +
                    "co2A INT," +
                    "co2TailpipeAGpm NUMERIC(17, 14)," +
                    "co2TailpipeGpm NUMERIC(18, 14)," +
                    "comb08 INT," +
                    "comb08U NUMERIC(7, 4)," +
                    "combA08 INT," +
                    "combA08U NUMERIC(7, 4)," +
                    "combE NUMERIC(7, 4)," +
                    "combinedCD NUMERIC(5, 4)," +
                    "combinedUF NUMERIC(5, 4)," +
                    "cylinders VARCHAR(2)," +
                    "displ VARCHAR(4)," +
                    "drive VARCHAR(26)," +
                    "engId INT," +
                    "eng_dscr VARCHAR(38)," +
                    "feScore INT," +
                    "fuelCost08 INT," +
                    "fuelCostA08 INT," +
                    "fuelType VARCHAR(27)," +
                    "fuelType1 VARCHAR(17)," +
                    "ghgScore INT," +
                    "ghgScoreA INT," +
                    "highway08 INT," +
                    "highway08U NUMERIC(7, 4)," +
                    "highwayA08 INT," +
                    "highwayA08U NUMERIC(7, 4)," +
                    "highwayCD NUMERIC(5, 4)," +
                    "highwayE NUMERIC(7, 4)," +
                    "highwayUF NUMERIC(5, 4)," +
                    "hlv INT," +
                    "hpv INT," +
                    "id INT," +
                    "lv2 INT," +
                    "lv4 INT," +
                    "make VARCHAR(34)," +
                    "model VARCHAR(47)," +
                    "mpgData VARCHAR(1)," +
                    "phevBlended VARCHAR(5)," +
                    "pv2 INT," +
                    "pv4 INT," +
                    "range INT," +
                    "rangeCity NUMERIC(7, 4)," +
                    "rangeCityA NUMERIC(6, 4)," +
                    "rangeHwy NUMERIC(7, 4)," +
                    "rangeHwyA NUMERIC(5, 3)," +
                    "trany VARCHAR(32)," +
                    "UCity NUMERIC(7, 4)," +
                    "UCityA NUMERIC(7, 4)," +
                    "UHighway NUMERIC(7, 4)," +
                    "UHighwayA NUMERIC(7, 4)," +
                    "VClass VARCHAR(34)," +
                    "year INT," +
                    "youSaveSpend INT," +
                    "guzzler VARCHAR(1)," +
                    "trans_dscr VARCHAR(15)," +
                    "tCharger VARCHAR(1)," +
                    "sCharger VARCHAR(1)," +
                    "atvType VARCHAR(14)," +
                    "fuelType2 VARCHAR(11)," +
                    "rangeA VARCHAR(11)," +
                    "evMotor VARCHAR(51)," +
                    "mfrCode VARCHAR(3)," +
                    "c240Dscr VARCHAR(16)," +
                    "charge240b NUMERIC(3, 2)," +
                    "c240bDscr VARCHAR(19)," +
                    "createdOn VARCHAR(28)," +
                    "modifiedOn VARCHAR(28)," +
                    "startStop VARCHAR(1)," +
                    "phevCity INT," +
                    "phevHwy INT," +
                    "phevComb INT)";


    public CarDatabase(Context context) {
        super(context, CAR_DATABASE_NAME, null, DATABASE_VERSION);
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
