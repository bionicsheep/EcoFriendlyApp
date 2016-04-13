package tunca.tom.ecofriendlyapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class ProgressFragment extends Fragment {

    ArrayList<Trip> processedTrips;
    TripDataProc tripDataProc;

    public ProgressFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);

        return rootView;
    }

    @Override
    public void onResume(){
        ((MainActivity)getActivity()).getNavigationView().getMenu().getItem(0).setChecked(true);
        super.onResume();
        startButtonClickListener();

        getTrips();
    }

    private void getTrips(){
        tripDataProc = new TripDataProc(getContext());
        processedTrips = tripDataProc.getProcessedTripsList();

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int emission = myPreferences.getInt("car_emission_value",592);

        TextView title = (TextView) getActivity().findViewById(R.id.change_title);
        TextView subtitle = (TextView) getActivity().findViewById(R.id.change_subtitle);
        TextView percentage = (TextView) getActivity().findViewById(R.id.percentage_change);

        double dEmission1, dEmission2;
        double tEmission1, tEmission2;
        double lastEmission, previousEmission;

        if(processedTrips.size() < 1){
            //no processed trips
            title.setText("No trips have been recorded or processed!");
            subtitle.setText("Check back here later or turn on service in settings!");
            percentage.setText("¯\\_(ツ)_/¯");
            percentage.setTextSize(80);
        }else if(processedTrips.size() > 1){
            Trip lastTrip = processedTrips.get(processedTrips.size() - 1);
            Trip previousTrip = processedTrips.get(processedTrips.size() - 2);

            dEmission1 = (int)(previousTrip.getDriveDistance() * (emission / 1000.0));
            dEmission2 = (int)(lastTrip.getDriveDistance() * (emission / 1000.0));
            tEmission1 = (int)(previousTrip.getTransitDistance() * MainActivity.TRANSIT_EMISSION);
            tEmission2 = (int)(lastTrip.getTransitDistance() * MainActivity.TRANSIT_EMISSION);

            lastEmission = dEmission2 + tEmission2;
            previousEmission = dEmission1 + tEmission1;

            double changePercentage = 0;

            if(previousEmission == 0){
                changePercentage = 100;
                changePercentage = Math.round(changePercentage);
            }
            else{
                changePercentage = (lastEmission / previousEmission) * 100;
                changePercentage = Math.round(changePercentage);
            }

            changePercentage = 45;

            if(changePercentage > 100){
                title.setText("Emissions have increased by");
                subtitle.setText("Try using public transport to decrease your footprint!");
                percentage.setText("+" + Double.toString(changePercentage - 100) + "%");
            }else{
                title.setText("Emissions have decreased by");
                subtitle.setText("Great job decreasing your carbon footprint!");
                percentage.setText("-" + Double.toString(100 -changePercentage) + "%");
            }



        }

    }

    private void startButtonClickListener(){
        Button button = (Button) getActivity().findViewById(R.id.more_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateActionBar(1, "History");
            }
        });
    }

}