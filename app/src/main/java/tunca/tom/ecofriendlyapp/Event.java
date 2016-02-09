package tunca.tom.ecofriendlyapp;


public class Event {

    private String time;
    private String date;
    private double latitude;
    private double longitude;
    private double velocity;
    private double accuracy;

    public Event(String date, String time, double latitude, double longitude, double velocity, double accuracy){
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.velocity = velocity;
        this.accuracy = accuracy;
    }

    public double getVelocity(){
        return velocity;
    }

    public double getAccuracy(){
        return accuracy;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public String getTime(){
        return time;
    }

    public String getDate(){
        return date;
    }
}
