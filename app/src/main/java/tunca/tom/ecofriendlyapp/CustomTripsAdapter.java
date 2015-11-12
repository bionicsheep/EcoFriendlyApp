package tunca.tom.ecofriendlyapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by aless_000 on 11/11/2015.
 */
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

        TextView tvName = (TextView) convertView.findViewById(R.id.tripDate);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tripDistance);

        tvName.setText(trip.date);
        tvHome.setText(trip.trip_distance);

        return convertView;
    }

}
