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
import android.widget.EditText;
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
    private SQLiteDatabase mDatabase;
    private ProgressDialog mProgDialog;

    private static final String MANUFACTURER_COL = "Manufacturer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.car_preferences);

        //Load last entered values for car
        setPreviousValues();

        //Add listeners for when you click on each setting
        setPreferenceClickListener();

        //initialize database
        initializeDatabase(getContext());

        //insert values
        initializeValues();
    }

    private void initializeValues(){
        //check if database was already populated, if so return
        Cursor mCursor = mDatabase.rawQuery("SELECT * FROM " + "vehicles", null);
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
                        mDatabase.execSQL(insertStmt);
                        getActivity().runOnUiThread(new Runnable(){
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

    private void initializeDatabase(Context context){
        CarDatabase mDatabaseHelper = new CarDatabase(context);
        mDatabase = mDatabaseHelper.getReadableDatabase();
    }

    private void setPreferenceClickListener(){
        //add listener to each preference to fire when it is clicked.
        //will create a popup with the hint, preference to be updated (itself), and the setting name corresponding to the fired preference
        carBrandPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                buildPopUp("Car brand",carBrandPref,"car_brand_value");
                return false;
            }
        });

        carModelPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                buildPopUp("Car model",carModelPref,"car_model_value");
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

        //applies last values, if none exist default to ABARTH 500 TODO
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        carBrandPref.setSummary(myPreferences.getString("car_brand_value","ABARTH"));
        carModelPref.setSummary(myPreferences.getString("car_model_value","500"));
        carYearPref.setSummary(myPreferences.getString("car_year_value","click to export"));
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

    }

    //creates a popup for when you click a preference.
    private void buildPopUp(String hint, Preference parent, String pref){
        final EditText txtUrl = new EditText(getContext());

        txtUrl.setHint(hint);

        //finalize variables so alert dialog can use them (has to be final)
        final Preference mParent = parent;
        final String mPref = pref;

        //create dialog
        new AlertDialog.Builder(getContext())
                .setView(txtUrl)
                //set confirm button actions
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //saves whatever preference you changed
                        SharedPreferences.Editor editor = myPreferences.edit();
                        mParent.setSummary(txtUrl.getText().toString());
                        editor.putString(mPref, txtUrl.getText().toString());
                        editor.commit();
                    }
                })
                //if you hit cancel
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                //show the dialog
                .show();
    }
}
