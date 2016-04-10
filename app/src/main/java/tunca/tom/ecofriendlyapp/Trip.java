package tunca.tom.ecofriendlyapp;

public class Trip {

    private String date;
    private double walkdistance;
    private double drivedistance;
    private double transitdistance;
    private double bikedistance;

    public Trip(String date, double drivedistance, double walkdistance, double bikedistance, double transitdistance) {
        this.date = date;
        this.drivedistance = drivedistance;
        this.walkdistance = walkdistance;
        this.bikedistance = bikedistance;
        this.transitdistance = transitdistance;
    }

    public String getDate(){
        return this.date;
    }

    public double getDriveDistance(){
        return this.drivedistance;
    }

    public double getWalkDistance(){
        return this.walkdistance;
    }

    public double getBikeDistance(){
        return this.bikedistance;
    }

    public double getTransitDistance(){
        return this.transitdistance;
    }
}