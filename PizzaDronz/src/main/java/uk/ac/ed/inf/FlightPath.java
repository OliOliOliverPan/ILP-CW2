package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class FlightPath {

    private String month;
    private String date;
    private LngLat currentPosition;
    private int remainingBattery;

    private NoFlyZone[] noFlyZones;
    private ArrayList<LngLat> flightPath;
    private ArrayList<LngLat> deliveryRestaurantList;

    public FlightPath(String month, String date, LngLat currentPosition, int remainingBattery) throws MalformedURLException, InvalidPizzaCombinationException {
        this.month = month;
        this.date = date;
        this.currentPosition = currentPosition;
        this.remainingBattery = remainingBattery;

        this.noFlyZones = NoFlyZone.getINSTANCE();


        this.deliveryRestaurantList = null;
        ArrayList<Order> validOrders = Order.getOrdersFromRestServer(month, date);
        for(Order o: validOrders){
            this.deliveryRestaurantList.add(o.findRestaurant().getCoordinate());
        }

    }
}
