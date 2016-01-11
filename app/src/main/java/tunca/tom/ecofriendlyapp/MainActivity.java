package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    private String mTitle = "Progress";
    private Fragment historyMapFragment;
    private FloatingActionButton fab;
    private MapHelper mMapHelper;
    private TripDataProcessor mTripDataProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapHelper = new MapHelper(getApplicationContext());
        mTripDataProcessor = new TripDataProcessor(getApplicationContext());
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            fab.hide();
            navigationView.getMenu().getItem(0).setChecked(true);
            getSupportActionBar().setTitle(mTitle);
            startProgressFragment();
        }
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
            startProgressFragment();
            mTitle = "Progress";
            getSupportActionBar().setTitle(mTitle);
        } else if (id == R.id.nav_history) {
            startTripsFragment();
            mTitle = "History";
            getSupportActionBar().setTitle(mTitle);
        } else if (id == R.id.nav_share) {
            //TODO
        } else if (id == R.id.nav_settings) {
            startSettingsFragment();
            mTitle = "Settings";

        } else if (id == R.id.nav_map_history){
            startMapHistoryFragment();
            mTitle = "Map History";
            getSupportActionBar().setTitle(mTitle);
            fab.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMapHelper.setGoogleMap(map);
        setDate(getDate());
    }

    @Override
    public void setDate(String date){
        mMapHelper.paintHistory(date);

        //mTripDataProcessor.loadData(date);
        //Log.d("driven distance", "" + mTripDataProcessor.getDriveDistance());
        //Log.d("walked distance", "" + mTripDataProcessor.getWalkDistance());
    }

    private void startSettingsFragment(){
        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
    }

    private void startProgressFragment() {
        Fragment progressFragment = new ProgressFragment();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, progressFragment).commit();
    }

    private void startTripsFragment(){
        Fragment historyFragment = new HistoryFragment();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, historyFragment).commit();
    }

    private void startMapHistoryFragment(){
        historyMapFragment = new HistoryMapFragment();
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, historyMapFragment).commit();
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
