package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Drone {

    public static final int BATTERY = 2000;

    private final LngLat startPosition = new LngLat(-3.186874, 55.944494);

    private LngLat currentPosition;
    private int remainingBattery;

    private Restaurant[] restaurants;

    private NoFlyZone[] noFlyZones;
    private ArrayList<LngLat> flightPath;
    private ArrayList<Restaurant> deliveryRestaurantList;



    public Drone(LngLat currentPosition, int remainingBattery, ArrayList<LngLat> flightPath, Order[] deliveryList, String month, String date) throws InvalidPizzaCombinationException, MalformedURLException {
        this.currentPosition = startPosition;
        this.remainingBattery = BATTERY;
        this.noFlyZones = NoFlyZone.getNoFlyZonesFromRestServer(new URL(NoFlyZone.noFlyZoneUrl));
        this.restaurants = Restaurant.getRestaurantsFromRestServer(new URL(Restaurant.restaurantUrl));
        this.flightPath = null;
        this.deliveryRestaurantList = Order.findRestaurant(this.restaurants, month, date);
    }
}
