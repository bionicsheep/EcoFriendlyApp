package tunca.tom.ecofriendlyapp;

import android.location.Location;

public class TripSeg {

    Event start;
    Event end;

    public TripSeg(Event start, Event end){
        this.start = start;
        this.end = end;
    }

    public Event getStart(){
        return start;
    }

    public Event getEnd(){
        return end;
    }

    public int getDuration() {
        String time1 = start.getTime();
        String time2 = end.getTime();

        int hour1 = Integer.parseInt(time1.substring(0, 2));
        int minute1 = Integer.parseInt(time1.substring(2, 4));
        int second1 = Integer.parseInt(time1.substring(4, 6));

        int hour2 = Integer.parseInt(time2.substring(0, 2));
        int minute2 = Integer.parseInt(time2.substring(2, 4));
        int second2 = Integer.parseInt(time2.substring(4, 6));

        int totTime1 = (hour1 * 60 * 60) + (minute1 * 60) + second1;
        int totTime2 = (hour2 * 60 * 60) + (minute2 * 60) + second2;

        return totTime2 - totTime1;
    }

    public double distanceDifference() {
        float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude(), results);

        return (double)results[0];
    }
}
