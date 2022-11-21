package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class Drone {

    public static final int BATTERY = 2000;

    private final LngLat startPosition = new LngLat(-3.186874, 55.944494);

    private LngLat currentPosition;
    private int remainingBattery;
    private ArrayList<LngLat> flightPath;
    private Order[] deliveryList;


    public Drone(LngLat currentPosition, int remainingBattery, ArrayList<LngLat> flightPath, Order[] deliveryList, String month, String date) throws InvalidPizzaCombinationException, MalformedURLException {
        this.currentPosition = startPosition;
        this.remainingBattery = BATTERY;
        this.flightPath = null;
        //this.deliveryList = Order.getOrdersFromRestServer(month, date);
    }
}
