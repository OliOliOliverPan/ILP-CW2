package uk.ac.ed.inf;

/**
 * Helper class for writing details of each move in daily delivery paths into json file
 */

public class JsonRoute {
    public String OrderNo;
    public double fromLongitude;
    public double fromLatitude;
    public double angle;
    public double toLongitude;
    public double toLatitude;

    public long ticksSinceStartOfCalculation;

    /**
     * Constructor of JsonRoute class
     *
     * @param orderNo          the order's order number
     * @param fromLongitude    the longitude of the drone before making a move
     * @param fromLatitude     the latitude of the drone before making a move
     * @param angle            the direction of the drone's move
     * @param toLongitude      the longitude of the drone after making a move
     * @param toLatitude       the latitude of the drone after making a move
     * @param  ticksSinceStartOfCalculation the computation time of making the step relative to the start of the whole day's delivery
     */
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
