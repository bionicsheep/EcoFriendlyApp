package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class SegmentFragment extends Fragment {

    private TripDataProc mTripData;
    private String date;

    public SegmentFragment() {
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getDate(){
        String mdate = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, date.length()); //insert backslashes
        return mdate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_segment, container, false);
        populateSegmentsList(rootView);
        return rootView;
    }

    private void populateSegmentsList(View rootView) {
        mTripData = new TripDataProc(getContext());
        ArrayList<TripSeg> arrayOfSegments = mTripData.getSegments(date);
        CustomSegmentsAdapter adapter = new CustomSegmentsAdapter(getContext(), arrayOfSegments);
        ListView listView = (ListView) rootView.findViewById(R.id.segments_listview);
        listView.setAdapter(adapter);
    }
}