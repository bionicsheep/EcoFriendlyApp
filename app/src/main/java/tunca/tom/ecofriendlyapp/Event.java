package tunca.tom.ecofriendlyapp;

/**
 * Created by aless_000 on 12/16/2015.
 */
public class Event {

    private String time;
    private String date;
    private double xCoor;
    private double yCoor;
    private double velocity;
    private double accuracy;

    public Event(String date, String time, double xCoor, double yCoor, double velocity, double accuracy){
        this.date = date;
        this.time = time;
        this.xCoor = xCoor;
        this.yCoor = yCoor;
        this.velocity = velocity;
        this.accuracy = accuracy;
    }

    public double getVelocity(){
        return velocity;
    }

    public double getAccuracy(){
        return accuracy;
    }

    public double getxCoor(){
        return xCoor;
    }

    public double getyCoor(){
        return yCoor;
    }

    public String getTime(){
        return time;
    }

    public String getDate(){
        return date;
    }
}
