package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class Drone {

    public static final int BATTERY = 2000;

    public static final LngLat START_POSITION = new LngLat(-3.186874, 55.944494);

    private LngLat currentPosition;
    private int remainingBattery;

    private Restaurant[] restaurants;

    private NoFlyZone[] noFlyZones;
    private ArrayList<LngLat> flightPath;
    private ArrayList<Restaurant> deliveryRestaurantList;



    public Drone(LngLat currentPosition, int remainingBattery, ArrayList<LngLat> flightPath, Order[] deliveryList, String month, String date) throws InvalidPizzaCombinationException, MalformedURLException {
        this.currentPosition = START_POSITION;
        this.remainingBattery = BATTERY;
        this.noFlyZones = NoFlyZone.getINSTANCE();
        this.restaurants = Restaurant.getINSTANCE();
        this.flightPath = null;
        this.deliveryRestaurantList = null;
//        for(int i = 0 ; i < this.deliveryRestaurantList.size(); i ++) {
//            correspondingRestaurants.add(result.get(i).findRestaurant());
//        }
    }
}
