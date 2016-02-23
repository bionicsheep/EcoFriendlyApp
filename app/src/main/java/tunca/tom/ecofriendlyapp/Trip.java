package tunca.tom.ecofriendlyapp;

public class Trip {

    private String date;
    private int walkdistance;
    private int drivedistance;
    private int transitdistance;
    private int bikedistance;

    public Trip(String date, int walkdistance, int drivedistance, int transitdistance, int bikedistance) {
        this.date = date;
        this.walkdistance = walkdistance;
        this.drivedistance = drivedistance;
        this.transitdistance = transitdistance;
        this.bikedistance = bikedistance;
    }

    public String getDate(){
        return this.date;
    }

    public int getWalkDistance(){
        return this.walkdistance;
    }

    public int getDriveDistance(){
        return this.drivedistance;
    }

    public int getTransitDistance(){
        return this.transitdistance;
    }

    public int getBikeDistance(){
        return this.bikedistance;
    }


}