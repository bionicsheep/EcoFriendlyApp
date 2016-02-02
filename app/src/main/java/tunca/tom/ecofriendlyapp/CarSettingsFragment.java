package tunca.tom.ecofriendlyapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.EditText;

public class CarSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    Preference carBrandPref;
    Preference carModelPref;
    Preference carYearPref;
    SharedPreferences myPreferences;

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
                buildPopUp("Car year",carYearPref,"car_year_value");
                return false;
            }
        });
    }

    //loads and applies last values
    private void setPreviousValues(){
        carBrandPref = findPreference("settings_brand");
        carModelPref = findPreference("settings_model");
        carYearPref = findPreference("settings_year");

        //applies last values, if none exist default to audi a8 2006
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        carBrandPref.setSummary(myPreferences.getString("car_brand_value","audi"));
        carModelPref.setSummary(myPreferences.getString("car_model_value","a8"));
        carYearPref.setSummary(myPreferences.getString("car_year_value","2006"));
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
