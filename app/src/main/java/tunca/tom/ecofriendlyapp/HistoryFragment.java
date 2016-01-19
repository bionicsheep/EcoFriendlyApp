package tunca.tom.ecofriendlyapp;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class HistoryFragment extends Fragment {

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        populateTripsList(rootView);
        testDistance();

        return rootView;
    }

    private void populateTripsList(View rootView) {
        ArrayList<Trip> arrayOfTrips = Trip.getTrips();
        CustomTripsAdapter adapter = new CustomTripsAdapter(getActivity().getApplicationContext(), arrayOfTrips);
        ListView listView = (ListView) rootView.findViewById(R.id.trips_listview);
        listView.setAdapter(adapter);
    }

    private void testDistance(){
        TripDataProc test = new TripDataProc(getContext());
    }
}