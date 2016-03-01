package tunca.tom.ecofriendlyapp;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



public class CustomTripsAdapter extends ArrayAdapter<Trip>{

    SharedPreferences myPreferences;

    public CustomTripsAdapter(Context context, ArrayList<Trip> trips) {
        super(context, 0, trips);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trip trip = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_trip, parent, false);
        }

        TextView tripDate = (TextView) convertView.findViewById(R.id.tripDateTitle);
        TextView driveDistance = (TextView) convertView.findViewById(R.id.tripDriveTitle);
        TextView walkDistance = (TextView) convertView.findViewById(R.id.tripWalkTitle);
        TextView transitDistance = (TextView) convertView.findViewById(R.id.tripTransitTitle);
        TextView bikeDistance = (TextView) convertView.findViewById(R.id.tripBikeTitle);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int emission = myPreferences.getInt("car_emission_value",592);

        String date = trip.getDate();
        date = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, date.length()); //insert backslashes

        tripDate.setText("Date: " + date);
        driveDistance.setText("Driven: " + trip.getDriveDistance() + " meters for " + (int)(trip.getDriveDistance() * (emission / 1000.0)) + " kg of CO2");
        walkDistance.setText("Walked: " + trip.getWalkDistance()+ " meters for " + trip.getWalkDistance() * MainActivity.WALKING_EMISSION + " kg of CO2");
        transitDistance.setText("Transit: " + trip.getTransitDistance()+ " meters for " + trip.getTransitDistance() * MainActivity.BUS_EMISSION + " kg of CO2");
        bikeDistance.setText("Biked: " + trip.getBikeDistance()+ " meters for " + trip.getBikeDistance() * MainActivity.BIKE_EMISSION + " kg of CO2");

        return convertView;
    }

}
