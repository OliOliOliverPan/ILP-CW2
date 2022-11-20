package uk.ac.ed.inf;

import java.util.ArrayList;

public class Drone {

    public static final int BATTERY = 2000;

    //private final LngLat startPosition;

    private LngLat currentPosition;
    private int remainingBattery;
    private ArrayList<LngLat> flightPath;
    private Order[] deliveryList;


    public Drone(LngLat currentPosition, int remainingBattery, ArrayList<LngLat> flightPath, Order[] deliveryList) {
        this.currentPosition = currentPosition;
        this.remainingBattery = remainingBattery;
        this.flightPath = flightPath;
        this.deliveryList = deliveryList;
    }
}
