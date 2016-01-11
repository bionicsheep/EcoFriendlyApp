package tunca.tom.ecofriendlyapp;

public class TripSegment{
    int start;
    int end;
    int type;

    public TripSegment(int start, int end, int type){
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getStart(){
        return start;
    }

    public int getEnd(){
        return end;
    }

    public int getType(){
        return type;
    }
}