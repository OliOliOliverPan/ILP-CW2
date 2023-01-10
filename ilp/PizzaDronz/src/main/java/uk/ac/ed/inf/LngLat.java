package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.ArrayList;



/**
 * Class defining the coordinate of a point and its required methods
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LngLat implements Comparable<LngLat> {


    /**
     * The longitude and latitude fields
     */
    private double lng;
    private double lat;

    /**
     * Fields below will be used when LngLat denotes position of the drone
     */

    //drone's previous position before reaching current position
    private LngLat previousPosition;
    private double f;

    //total distance cost from starting position to current position
    private double g;
    //euclidean distance to destination point
    private double h;




    /**
     * Constructor of the coordinate of a point
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
     * @return       the distance between current point and that specific point
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
     * @return the coordinate of the drone after moving on that input compass direction
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

    /**
     * A method to round a double value to six decimal places
     *
     * @param  d the double value to be rounded with
     * @return rounded value
     */
    public static double roundToSixDecimals(double d){
        return (double) Math.round(d * 1000000) / 1000000;
    }



    /**
     * A method to scale up the longitude and latitude of a coordinate to integers in order to
     * avoid the long computation time of high precision double value calculation
     *
     * @param  point the coordinate to be scaled up with
     * @return the coordinate after being scaled up
     */
    public static LngLat magnify(LngLat point){
        double update_lng = Math.round(point.lng * 100000);
        double update_lat = Math.round(point.lat * 100000);
        return new LngLat(update_lng,update_lat);
    }


    /**
     * This method will be used when LngLat denotes current position of the drone
     * The method is to determine whether the drone's current position is close to any of the given
     * no-fly-zone areas
     *
     * @param  points a discrete number of points that construct the approximate contour of no-fly-zones
     *                which will be calculated distance with the drone's current position
     * @return true if the distance between the drone and a point in points is below the threshold value
     *         false otherwise
     */
    public boolean closeToNoFlyZone(ArrayList<LngLat> points) {

        //scale up the coordinate of the drone to integers
        LngLat magnifiedCoordinate = magnify(this);

        double shortestDistance = 1000000;
        for(LngLat point: points) {
            if ((point.lng - magnifiedCoordinate.lng) * (point.lng - magnifiedCoordinate.lng) +
                    (point.lat - magnifiedCoordinate.lat) * (point.lat - magnifiedCoordinate.lat) < shortestDistance) {

                shortestDistance = Math.sqrt((point.lng - magnifiedCoordinate.lng) * (point.lng - magnifiedCoordinate.lng) +
                        (point.lat - magnifiedCoordinate.lat) * (point.lat - magnifiedCoordinate.lat));
            }
        }

        //threshold value is square root 10
        return shortestDistance < Math.sqrt(10);
    }


    //Comparable interface is applied when LngLat denotes drone's position
    //The comparison will be called when finding the shortest path using a* algorithm
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



    //getters and setters for private fields of LngLat class
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



}