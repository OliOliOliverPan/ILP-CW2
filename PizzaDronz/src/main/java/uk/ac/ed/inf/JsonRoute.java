package uk.ac.ed.inf;

public class JsonRoute {
    public String OrderNo;
    public double fromLongitude;
    public double fromLatitude;
    public double angle;
    public double toLongitude;
    public double toLatitude;

    public long ticksSinceStartOfCalculation;

    public JsonRoute(String orderNo, double fromLongitude, double fromLatitude, double angle, double toLongitude, double toLatitude, long ticksSinceStartOfCalculation) {
        OrderNo = orderNo;
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
        this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
    }



}