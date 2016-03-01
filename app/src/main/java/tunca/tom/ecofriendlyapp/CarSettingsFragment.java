package tunca.tom.ecofriendlyapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class CarSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    Preference carBrandPref;
    Preference carModelPref;
    Preference carYearPref;
    SharedPreferences myPreferences;
    private SQLiteDatabase mLongDatabase;
    private ProgressDialog mProgDialog;

    private String selectedMake;
    private String selectedModel;
    private String[] makeArray;
    private String[] modelArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.car_preferences);

        //initialize database
        initializeDatabase(getContext());

        //Load last entered values for car
        setPreviousValues();

        //initializeMake
        initializeMake();

        //initializeModel()
        initializeModel();

        //Add listeners for when you click on each setting
        setPreferenceClickListener();

        //insert values
        insertValues();
    }

    private void insertValues(){
        //check if database was already populated, if so return
        Cursor mCursor = mLongDatabase.rawQuery("SELECT * FROM " + "vehicles", null);
        if(mCursor.getCount() != 0){
            return;
        }

        //otherwise insert all values and update progress bar
        mProgDialog = new ProgressDialog(getContext());
        mProgDialog.setMessage("Loading car models");
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgDialog.setCancelable(false);
        mProgDialog.setIndeterminate(false);
        mProgDialog.setProgress(0);
        mProgDialog.setMax(36945);
        mProgDialog.show();
        new Thread()
        {
            public void run()
            {
                try {
                    int result = 0;
                    // Open the resource
                    InputStream insertsStream = getContext().getResources().openRawResource(R.raw.car_creation_script);
                    BufferedReader insertReader = new BufferedReader(new InputStreamReader(insertsStream));

                    // Iterate through lines (assuming each insert has its own line and theres no other stuff)
                    while (insertReader.ready()) {
                        String insertStmt = insertReader.readLine();
                        mLongDatabase.execSQL(insertStmt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgDialog.incrementProgressBy(1);
                            }
                        });
                        result++;
                    }
                    insertReader.close();
                }
                catch(Exception ex){

                }

                mProgDialog.dismiss();
            }
        }.start();
    }

    private void initializeMake(){
        String[] columns = {"make"};
        //query to get list
        Cursor c = mLongDatabase.query(
                true, //unique values
                "vehicles", //table
                columns, //only "make"
                null,
                null,
                null,
                null,
                "make ASC", //"sort alphabetically"
                null);

        makeArray = new String[c.getCount()];
        int makeRow = c.getColumnIndex("make");
        int index = 0;

        //go through cursor of returned values from query and add to array
        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            makeArray[index] = c.getString(makeRow);
            index++;
        }
        c.close();
    }

    //initializes an array of all models based on the make/brand selected
    private void initializeModel(){
        String[] columns = {"model"};
        //query to get list
        Cursor c = mLongDatabase.query(
                true, //unique values
                "vehicles", //table
                columns, //only "model"
                "make= '" + selectedMake + "'", //only pull up matching the make/brand
                null,
                null,
                null,
                "make ASC", //alphabetical sort
                null);

        modelArray = new String[c.getCount()];
        int modelRow = c.getColumnIndex("model");
        int index = 0;

        //go through cursor of returned values from query and add to array
        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            modelArray[index] = c.getString(modelRow);
            index++;
        }
        c.close();
    }

    private int getCO2(){
        String[] columns = {"model","make","co2TailpipeGpm"};
        //query to get list
        Cursor c = mLongDatabase.query(
                true, //unique values
                "vehicles", //table
                columns, //only "model"
                "make= '" + selectedMake + "'", //only pull up matching the make/brand
                null,
                null,
                null,
                "make ASC", //alphabetical sort
                null);

        int co2Row = c.getColumnIndex("co2TailpipeGpm");
        int modelRow = c.getColumnIndex("model");
        int emission = 0;

        //go through cursor of returned values from query and add to array
        for(c.moveToFirst();!c.isAfterLast(); c.moveToNext()){
            if(c.getString(modelRow) == selectedModel){
                emission = Integer.parseInt(c.getString(co2Row));
            }
        }
        c.close();
        return emission;
    }

    private void initializeDatabase(Context context){
        CarDatabase mDatabaseHelper = new CarDatabase(context);
        mLongDatabase = mDatabaseHelper.getReadableDatabase();
    }

    private void setPreferenceClickListener(){
        //add listener to each preference to fire when it is clicked.
        //will create a popup with the hint, preference to be updated (itself), and the setting name corresponding to the fired preference
        carBrandPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                buildMakePopUp();
                return false;
            }
        });

        carModelPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                buildModelPopUp();
                return false;
            }
        });
        carYearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                exportDatabase();
                return false;
            }
        });

    }

    //exports database of cars
    private void exportDatabase(){
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()){
                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "EcoFriendlyApp");

                if (!folder.exists()) {
                    folder.mkdir();
                }
                String currentDBPath = "/data/tunca.tom.ecofriendlyapp/databases/vehicles.db";
                String backupDBPath = "/EcoFriendlyApp/vehicles.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                Toast.makeText(getContext(), "Export Successful", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    //loads and applies last values
    private void setPreviousValues(){
        carBrandPref = findPreference("settings_brand");
        carModelPref = findPreference("settings_model");
        carYearPref = findPreference("settings_year");

        //applies last values, if none exist default to Audi R8
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        selectedMake = myPreferences.getString("car_brand_value","Audi");
        selectedModel = myPreferences.getString("car_model_value","R8");

        carBrandPref.setSummary(selectedMake);
        carModelPref.setSummary(selectedModel);
        carYearPref.setSummary("click to export");
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //nada
    }

    //popup when selecting model
    private void buildModelPopUp(){
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("Model");
        b.setItems(modelArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //local save for pulling
                selectedModel = modelArray[which];
                //saves whatever preference you changed
                SharedPreferences.Editor editor = myPreferences.edit();
                editor.putString("car_model_value", selectedModel);
                editor.commit();

                //update actual text
                carModelPref.setSummary(selectedModel);

                //dismiss popup
                dialog.dismiss();
            }

        });
        b.show();
    }

    //popup when selecting make/brand
    private void buildMakePopUp(){
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("Brand");
        b.setItems(makeArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //local save for pulling
                selectedMake = makeArray[which];
                //saves whatever preference you changed
                SharedPreferences.Editor editor = myPreferences.edit();
                editor.putString("car_brand_value", selectedMake);
                editor.commit();
                //update actual text
                carBrandPref.setSummary(selectedMake);

                //----------------------------------------------------------------
                //reload all models for new selected brand and set to first option
                //----------------------------------------------------------------
                initializeModel();
                selectedModel = modelArray[0];
                //saves whatever preference you changed
                editor = myPreferences.edit();
                editor.putString("car_model_value", selectedModel);
                editor.commit();

                //load CO2 emissions for that car
                editor = myPreferences.edit();
                editor.putInt("car_co2_value", getCO2());
                editor.commit();

                //update actual text
                carModelPref.setSummary(selectedModel);

                //dismiss popup
                dialog.dismiss();
            }

        });
        b.show();
    }
}
