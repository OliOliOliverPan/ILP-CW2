package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.awt.geom.Path2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * Class defining the longitude and latitude of the drone and its required methods
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LngLat implements Comparable<LngLat> {


    /**
     * The longitude and latitude fields of the drone
     */
    private double lng;
    private double lat;
    private LngLat previousPosition;
    private double f;
    private double g;
    private double h;




    /**
     * Constructor of the drone's position
     *
     * @param lng the longitude of the drone
     * @param lat the latitude of the drone
     */
    public LngLat(@JsonProperty("longitude")double lng, @JsonProperty("latitude")double lat){
        this.lng = lng;
        this.lat = lat;

        this.f = 0;
        this.g = 0;
        this.h = 0;

    }

    /**
     * Obtain coordinates of the central area corners and determine if the drone is inside the central area
     *
     * @return true if the drone's coordinate is within the central area,
     *         false if it is outside central area or on the edge of the central area
     *
     */
    public boolean inCentralArea(){

        // Extract longitude and latitude data from the object of CentralArea class
        ArrayList<Double> figuresArrayList = CentralArea.getInstance().deriveCornerData();

        double minimum_lng = figuresArrayList.get(0);
        double maximum_lng = figuresArrayList.get(1);
        double minimum_lat = figuresArrayList.get(2);
        double maximum_lat = figuresArrayList.get(3);


        return (this.lng > minimum_lng) && (this.lng < maximum_lng)
                && (this.lat > minimum_lat) && (this.lat < maximum_lat);
    }



    /**
     * Compute the distance between current position of the drone and another specific point using Pythagorean Theorem
     *
     * @param  point the point to be calculated distance with
     * @return the distance between current position of the drone and that specific point
     * @throws NullPointerException if the given point is null
     */
    public double distanceTo(LngLat point){

        if(point == null){
            throw new NullPointerException("The given point is null");
        }

        double lng_difference = point.lng - this.lng;
        double lat_difference = point.lat - this.lat;

        // Apply Pythagorean Theorem to calculate the distance between the drone and the point
        return Math.pow((lng_difference * lng_difference + lat_difference * lat_difference),0.5);
    }

    /**
     * Determine if the distance between current position of the drone and another specific point
     * is strictly less than 0.00015 degrees
     *
     * @param  point the point to be determined with
     * @return true if the distance between the drone and the point is less than 0.00015 degrees,
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
                return new LngLat(this.lng + 0.00015 * Math.cos(Math.toRadians(angle)),
                                   this.lat + 0.00015 * Math.sin(Math.toRadians(angle)));
            }
        }
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





