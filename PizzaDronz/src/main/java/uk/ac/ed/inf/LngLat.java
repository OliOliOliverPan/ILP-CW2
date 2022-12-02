package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;



import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;



/**
 * Class defining the longitude and latitude of a point and its required methods
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LngLat implements Comparable<LngLat> {


    /**
     * The longitude and latitude fields
     */
    private double lng;
    private double lat;


    private LngLat previousPosition;
    private double f;
    private double g;
    private double h;




    /**
     * Constructor of the coordinate of the point
     *
     * @param lng the longitude
     * @param lat the latitude
     */
    public LngLat(@JsonProperty("longitude")double lng, @JsonProperty("latitude")double lat){
        this.lng = lng;
        this.lat = lat;

        this.f = 0;
        this.g = 0;
        this.h = 0;

    }

    /**
     * Determine if the coordinate of a point is inside the central area
     *
     * @param minimum_lng longitude of the bottom right corner of central area
     * @param minimum_lat latitude of the bottom right corner of central area
     * @param maximum_lng longitude of the upper left corner of central area
     * @param maximum_lat latitude of the upper left corner of central area
     *
     * @return true if the given coordinate is within the central area,
     *         false if it is outside central area or on the edge of the central area
     *
     */
    public boolean inCentralArea(double minimum_lng, double minimum_lat, double maximum_lng, double maximum_lat){



        return (this.lng > minimum_lng) && (this.lng < maximum_lng)
                && (this.lat > minimum_lat) && (this.lat < maximum_lat);
    }



    /**
     * Compute the distance between current point and another specific point using Pythagorean Theorem
     *
     * @param  point the point to be calculated distance with
     * @return the distance between current point and that specific point
     * @throws NullPointerException if the given point is null
     */
    public double distanceTo(LngLat point){

        if(point == null){
            throw new NullPointerException("The given point is null");
        }

        double lng_difference = point.lng - this.lng;
        double lat_difference = point.lat - this.lat;

        // Apply Pythagorean Theorem to calculate the distance between two points
        return Math.sqrt(lng_difference * lng_difference + lat_difference * lat_difference);
    }

    /**
     * Determine if the distance between current coordinate and another specific point
     * is strictly less than 0.00015 degrees
     *
     * @param  point the point to be determined with
     * @return true if the distance between two points is less than 0.00015 degrees,
     *         false if not
     *
     * @throws NullPointerException if the given point is null
     */
    public boolean closeTo(LngLat point){

        if(point == null){
            throw new NullPointerException("The given point is null");
        }

        return this.distanceTo(point) < 0.00015;
    }


    /**
     * This method is used when the LngLat object denotes the coordinate of the drone
     * Given a compass direction, find the coordinate of the drone after it makes a move along with this direction
     * with Pythagorean distance of 0.00015 degrees, or the drone will hover if the input direction is null
     *
     * @param  direction the compass direction that the drone will move on
     * @return the coordinate of the drone after moving / hovering on that input compass direction
     *
     *
     * @throws IllegalArgumentException if the given point is not a valid compass direction
     */
    public LngLat nextPosition(Direction direction){

        // The case where the drone's next step is to hover,
        // the drone's coordinate will remain unchanged
        if(direction == null){
            return new LngLat(this.lng, this.lat);
        }
        else {
            double angle = direction.getAngle();

            // According to the definition of 16 compass directions,
            // we can determine that all of their angles are divisible by 22.5 degrees
            if(angle % 22.5 != 0){
                throw new IllegalArgumentException("The given direction is not a valid compass direction");
            }
            else {
                return new LngLat(roundToSixDecimals(this.lng + 0.00015 * Math.cos(Math.toRadians(angle))),
                        roundToSixDecimals(this.lat + 0.00015 * Math.sin(Math.toRadians(angle))));
            }
        }
    }

    public static double roundToSixDecimals(double d){
        return (double) Math.round(d * 1000000) / 1000000;
    }

    public static LngLat magnify(LngLat point){
        double update_lng = Math.round(point.lng * 100000);
        double update_lat = Math.round(point.lat * 100000);
        return new LngLat(update_lng,update_lat);
    }



    public boolean inNoFlyZone() throws MalformedURLException {

        NoFlyZone[] noFlyZones = NoFlyZone.getINSTANCE();

        boolean inNFZ = false;
        for(NoFlyZone nfz: noFlyZones) {
            if(nfz.getNoFlyZoneArea().contains(this.getLng(), this.getLat())){
                inNFZ = true;
                break;
            }
        }

        return inNFZ;
    }

    public boolean closeToNoFlyZone(ArrayList<LngLat> points) throws MalformedURLException {
        LngLat magnifiedCoordinate = magnify(this);

        //NoFlyZone[] noFlyZones = NoFlyZone.getINSTANCE();

        double shortestDistanceSquared = 1000000;
        for(LngLat point: points) {
            if ((point.lng - magnifiedCoordinate.lng) * (point.lng - magnifiedCoordinate.lng) +
                    (point.lat - magnifiedCoordinate.lat) * (point.lat - magnifiedCoordinate.lat) < shortestDistanceSquared) {

                shortestDistanceSquared = (point.lng - magnifiedCoordinate.lng) * (point.lng - magnifiedCoordinate.lng) +
                        (point.lat - magnifiedCoordinate.lat) * (point.lat - magnifiedCoordinate.lat);
            }
        }
        return shortestDistanceSquared < (0.00015 * 100000) * (0.00015 * 100000) ;
    }


    @Override
    public int compareTo(LngLat o) {
        if(this.getF() < o.getF()){
            return -1;
        }
        else if(this.getF() > o.getF()){
            return 1;
        }
        else{
            return 0;
        }
    }

    public double getLng() {
        return this.lng;
    }

    public double getLat() {
        return this.lat;
    }

    public LngLat getPreviousPosition() {
        return previousPosition;
    }

    public double getF() {
        return f;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public void setPreviousPosition(LngLat previousPosition) {
        this.previousPosition = previousPosition;
    }

    public void setF(double f) {
        this.f = f;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

    public static void main(String[] args) throws MalformedURLException {
        NoFlyZone[] noFlyZones = NoFlyZone.getINSTANCE();

        LngLat point = new LngLat(-3.1882,55.9447);

        System.out.println(point.inNoFlyZone());

    }


}