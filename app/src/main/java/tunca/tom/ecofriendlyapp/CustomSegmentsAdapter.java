package tunca.tom.ecofriendlyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomSegmentsAdapter extends ArrayAdapter<TripSeg> {

    public CustomSegmentsAdapter(Context context, ArrayList<TripSeg> segments) {
        super(context, 0, segments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TripSeg segment = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_segment, parent, false);
        }

        Event e1 = segment.getStart();
        Event e2 = segment.getEnd();
        int mode = segment.getMode();

        TextView segmentTime = (TextView) convertView.findViewById(R.id.segmentTimeTitle);
        TextView segmentLocStart = (TextView) convertView.findViewById(R.id.segmentLocStartTitle);
        TextView segmentLocEnd = (TextView) convertView.findViewById(R.id.segmentLocEndTitle);
        TextView segmentMode = (TextView) convertView.findViewById(R.id.segmentModeTitle);

        String timeStart = e1.getTime();
        timeStart = timeStart.substring(0,2) + ":" + timeStart.substring(2,4);
        String timeEnd = e2.getTime();
        timeEnd = timeEnd.substring(0,2) + ":" + timeEnd.substring(2,4);

        segmentTime.setText("Trip went from " + timeStart + " to " + timeEnd);

        switch(mode){
            case 0:
                segmentMode.setText("via driving");
                break;
            case 1:
                segmentMode.setText("via walking");
                break;
            case 2:
                segmentMode.setText("via bicycling");
                break;
            case 3:
                segmentMode.setText("via transit");
                break;
        }

        segmentLocStart.setText("Trip started at: (" + e1.getLatitude() + ", " +  e1.getLongitude() + ")");
        segmentLocEnd.setText("Trip ended at: (" + e2.getLatitude() + ", " +  e2.getLongitude() + ")");

        return convertView;
    }

}
