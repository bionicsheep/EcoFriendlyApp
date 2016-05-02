package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HistoryMapFragment.OnGoogleMapFragmentListener, DatePickerFragment.OnDateSelectedListener {

    public static final int WALKING_EMISSION = 0;
    public static final double TRANSIT_EMISSION = 1.725;
    public static final int BIKE_EMISSION = 0;

    private String mTitle = "Progress";
    private Fragment historyMapFragment;
    private FloatingActionButton fab;
    private MapHelper mMapHelper;
    private TripDataProc mTripDataProc;
    private String[] dates;
    private FragmentManager mFragmentManager;

    private int loadingIndex = 0;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapHelper = new MapHelper(getApplicationContext());
        mTripDataProc = new TripDataProc(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fab.hide();

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                ProgressFragment progressFragment = (ProgressFragment)(mFragmentManager.findFragmentByTag("progress_fragment"));
                HistoryFragment historyFragment = (HistoryFragment)(mFragmentManager.findFragmentByTag("history_fragment"));
                HistoryMapFragment historyMapFragment = (HistoryMapFragment)(mFragmentManager.findFragmentByTag("history_map_fragment"));
                SettingsFragment settingsFragment = (SettingsFragment)(mFragmentManager.findFragmentByTag("settings_fragment"));
                SegmentFragment segmentFragment = (SegmentFragment)(mFragmentManager.findFragmentByTag("segment_fragment"));
                if (progressFragment != null && progressFragment.isVisible()) {
                    getSupportActionBar().setTitle("Progress");
                }
                if (historyFragment != null && historyFragment.isVisible()) {
                    getSupportActionBar().setTitle("Emission's List");
                }
                if (historyMapFragment != null && historyMapFragment.isVisible()) {
                    getSupportActionBar().setTitle("Map History");
                }
                if (settingsFragment != null && settingsFragment.isVisible()) {
                    getSupportActionBar().setTitle("Settings");
                }
                if (segmentFragment != null && segmentFragment.isVisible()) {
                    getSupportActionBar().setTitle(segmentFragment.getDate());
                }
            }
        });

        if (savedInstanceState == null) {
            Log.d("MainActivity","test");
            navigationView.getMenu().getItem(0).setChecked(true);
            getSupportActionBar().setTitle(mTitle);
            startProgressFragment();

            //dates = mTripDataProc.loadHistory(getDate());
            //loadNext();
        }
    }

    public NavigationView getNavigationView(){
        return navigationView;
    }

    public void loadNext(){
        if(loadingIndex < dates.length){
            mTripDataProc = new TripDataProc(this);
            mTripDataProc.loadData(dates[loadingIndex]);
        }
        loadingIndex++;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_progress) {
            updateActionBar(0, "Progress");
        } else if (id == R.id.nav_history) {
            updateActionBar(1, "History");
        } else if (id == R.id.nav_share) {
            updateActionBar(2, "Share");
        } else if (id == R.id.nav_settings) {
            updateActionBar(3, "Settings");
        } else if (id == R.id.nav_map_history){
            updateActionBar(4, "Map History");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    //used in the history map fragment to mark spots on map by using map helper
    public void onMapReady(GoogleMap map) {
        mMapHelper.setGoogleMap(map);
        setDate(getDate());
    }

    public void setDate(String date){
        mMapHelper.paintHistory(date);
    }

    public void updateActionBar(int fragment, String mTitle){
        if(fragment < 4 && navigationView.getMenu().getItem(fragment).isChecked()){
            return;
        }
        getSupportActionBar().setTitle(mTitle);
        switch(fragment){
            case 0:
                startProgressFragment();
                fab.hide();
                break;
            case 1:
                startTripsFragment();
                fab.hide();
                break;
            case 2:
                //share
                break;
            case 3:
                startSettingsFragment();
                fab.hide();
                break;
            case 4:
                startMapHistoryFragment();
                fab.show();
                break;
            case 5:
                startSegmentFragment(mTitle);
                fab.hide();
                break;
        }
    }

    private void startProgressFragment() {
        Fragment progressFragment = new ProgressFragment();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, progressFragment, "progress_fragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void startTripsFragment(){
        Fragment historyFragment = new HistoryFragment();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, historyFragment, "history_fragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void startMapHistoryFragment(){
        historyMapFragment = new HistoryMapFragment();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, historyMapFragment, "history_map_fragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void startSettingsFragment(){
        SettingsFragment settingsFragment = new SettingsFragment();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, settingsFragment, "settings_fragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void startSegmentFragment(String date){
        String mdate = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, date.length()); //insert backslashes
        getSupportActionBar().setTitle(mdate);
        SegmentFragment segmentFragment = new SegmentFragment();
        segmentFragment.setDate(date);
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, segmentFragment, "segment_fragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private String getDate(){
        Calendar mCalendar = Calendar.getInstance();
        String month = String.format("%02d", (mCalendar.get(Calendar.MONTH) + 1));
        String day = String.format("%02d",mCalendar.get(Calendar.DAY_OF_MONTH));
        String year = String.format("%02d",mCalendar.get(Calendar.YEAR));

        String date = month + day + year;

        return date;
    }
}
