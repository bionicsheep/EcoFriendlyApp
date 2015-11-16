package tunca.tom.ecofriendlyapp;


import java.util.ArrayList;

public class Trip {
    public String date;
    public String trip_distance;
    //additional trip data will be added later

    public Trip(String date, String trip_distance) {
        this.date = date;
        this.trip_distance = trip_distance;
    }

    //for demo, will update when data collection is done
    public static ArrayList<Trip> getTrips() {
        ArrayList<Trip> trips = new ArrayList<Trip>();
        trips.add(new Trip("11/2/2015", "7.5 Miles"));
        trips.add(new Trip("11/3/2015", "6.3 Miles"));
        trips.add(new Trip("11/4/2015", "7.6 Miles"));
        trips.add(new Trip("11/4/2015", "23.5 Miles"));
        trips.add(new Trip("11/5/2015", "11.2 Miles"));
        trips.add(new Trip("11/7/2015", "1.2 Miles"));
        trips.add(new Trip("11/10/2015", "3.6 Miles"));
        trips.add(new Trip("11/12/2015", "7.1 Miles"));
        trips.add(new Trip("11/12/2015", "11.2 Miles"));
        trips.add(new Trip("11/14/2015", "1.2 Miles"));
        trips.add(new Trip("11/15/2015", "3.6 Miles"));
        trips.add(new Trip("11/19/2015", "7.1 Miles"));
        return trips;
    }
}