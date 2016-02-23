package tunca.tom.ecofriendlyapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class CustomTripsAdapter extends ArrayAdapter<Trip>{
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

        String date = trip.getDate();
        date = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, date.length()); //insert backslashes

        tripDate.setText("Date: " + date);
        driveDistance.setText("Driven meters: " + trip.getDriveDistance());
        walkDistance.setText("Walked meters: " + trip.getWalkDistance());
        transitDistance.setText("Transite meters: " + trip.getTransitDistance());
        bikeDistance.setText("Biked Meters: " + trip.getBikeDistance());

        return convertView;
    }

}
