package tunca.tom.ecofriendlyapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        boolean isRunning = isMyServiceRunning(TripMonitor.class, getContext());
        mEditor.putBoolean("service_enabled", isRunning);
        mEditor.apply();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.main_preferences);
        setPreferenceClickListener();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("service_enabled")) {
            boolean serviceStatus = isMyServiceRunning(TripMonitor.class, getContext());
            Intent mIntent = new Intent(getContext(), TripMonitor.class);
            Log.d("test","" + serviceStatus);
            if(!serviceStatus){
                getContext().startService(mIntent);
            }else{
                getContext().stopService(mIntent);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager activityManager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }

    private void setPreferenceClickListener(){
        Preference exportPref = findPreference("export_database");
        exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                exportDatabase();
                return true;
            }
        });

        Preference carPref = findPreference("car_model");
        carPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                CarSettingsFragment settingsFragment = new CarSettingsFragment();
                FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                mFragmentManager.beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
                return true;
            }
        });


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
                String currentDBPath = "/data/tunca.tom.ecofriendlyapp/databases/LocationData.db";
                String currentDBJournalPath = "/data/tunca.tom.ecofriendlyapp/databases/LocationData.db-journal";
                String backupDBPath = "/EcoFriendlyApp/LocationData.db";
                String backupDBJournalPath = "/EcoFriendlyApp/LocationData.db-journal";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                File currentDBJournal = new File(data, currentDBJournalPath);
                File backupDBJournal = new File(sd, backupDBJournalPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src = new FileInputStream(currentDBJournal).getChannel();
                    dst = new FileOutputStream(backupDBJournal).getChannel();
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
}