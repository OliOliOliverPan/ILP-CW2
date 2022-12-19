package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Drone {

    public static final int BATTERY = 2000;

    public static final LngLat START_POSITION = new LngLat(-3.186874, 55.944494);

    private String month;
    private String date;
    private LngLat currentPosition;
    private int remainingBattery;
    private ArrayList<Order> validOrders;


    private NoFlyZone[] noFlyZones;
    private HashMap<String, ArrayList<LngLat>> paths;
    private HashMap<String, ArrayList<Double>> angles;
    private HashMap<String, ArrayList<Long>> times;

    //Storing the route to each restaurant
    private HashMap<Restaurant, ArrayList<LngLat>> restaurantPath;



    private long totalRunningTime;



    public Drone(String month, String date) throws MalformedURLException {
        this.month = month;
        this.date = date;
        this.currentPosition = START_POSITION;
        this.remainingBattery = BATTERY;
        this.noFlyZones = NoFlyZone.getINSTANCE();

        this.paths = new HashMap<>();
        this.angles = new HashMap<>();
        this.times = new HashMap<>();

        this.validOrders = Order.getOrdersFromRestServer(month, date);


        this.totalRunningTime = 0;

    }


    public void planDailyRoute() throws MalformedURLException {
        for(Order o: this.validOrders){
            String orderNo = o.getOrderNo();
            FlightPath fp = new FlightPath(Drone.START_POSITION, o);

            fp.aStarPathFinding(o);
            ArrayList<LngLat> route = fp.getRoute();
            ArrayList<Double> directions = fp.getAngles();

            if(route.size() + 2 <= this.remainingBattery){
                this.remainingBattery = this.remainingBattery - route.size() - 2;
                o.setOrderStatus(OrderOutcome.Delivered);

                this.paths.put(orderNo,route);
                this.angles.put(orderNo,directions);

                ArrayList<Long> eachStepTime = fp.getEachStepTime();
                ArrayList<Long> relativeEachStepTime = new ArrayList<>();

                for(int i = 0; i < eachStepTime.size(); i++){
                    relativeEachStepTime.add(eachStepTime.get(i) + this.totalRunningTime);
                }
                this.times.put(orderNo,relativeEachStepTime);

                this.totalRunningTime += fp.getTotalTime();
            }
            else{
                break;
            }
        }
    }

    public HashMap<String, ArrayList<LngLat>> getPaths() {
        return paths;
    }

    public HashMap<String, ArrayList<Double>> getAngles() {
        return angles;
    }

    public HashMap<String, ArrayList<Long>> getTimes() {
        return times;
    }

//    public void setTimes(HashMap<String, ArrayList<Long>> times) {
//        this.times = times;
//    }

    public ArrayList<Order> getValidOrders() {
        return validOrders;
    }

    public int getRemainingBattery() {
        return remainingBattery;
    }

    //    public static void main(String[] args) throws InvalidPizzaCombinationException, MalformedURLException {
//        Drone drone = new Drone("04","15");
//        drone.planDailyRoute();
//        System.out.println(drone.remainingBattery);
//        System.out.println(drone.totalRunningTime);
//
//        int count = 0;
//        for(Order o: drone.validOrders){
//            if(o.getOrderStatus() == OrderOutcome.Delivered) count += drone.angles.get(o.getOrderNo()).size();
//
//            //System.out.println(o.getOrderStatus());
//        }
//        System.out.println(count);
//    }


}