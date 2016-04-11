package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    TripDataProc mTripData;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getNavigationView().getMenu().getItem(1).setChecked(true);
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        populateTripsList(rootView);
        return rootView;
    }

    private void populateTripsList(View rootView) {
        mTripData = new TripDataProc(getContext());
        final ArrayList<Trip> arrayOfTrips = mTripData.getProcessedTripsList();
        CustomTripsAdapter adapter = new CustomTripsAdapter(getContext(), arrayOfTrips);
        ListView listView = (ListView) rootView.findViewById(R.id.trips_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity)getActivity()).updateActionBar(5,arrayOfTrips.get(position).getDate());
            }
        });
    }
}