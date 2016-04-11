package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        populateTripsList(rootView);
        return rootView;
    }

    private void populateTripsList(View rootView) {
        ArrayList<Trip> arrayOfTrips = ((MainActivity)getActivity()).mCompletedTripsList; //will change to another database in future
        CustomTripsAdapter adapter = new CustomTripsAdapter(getContext(), arrayOfTrips);
        ListView listView = (ListView) rootView.findViewById(R.id.trips_listview);
        listView.setAdapter(adapter);
    }
}