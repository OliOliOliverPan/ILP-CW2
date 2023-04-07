package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class defining drone and helper functions for daily path-planning
 */
public class Drone {

    //drone's initial battery and starting point(Appleton Tower) at the start of the day
    public static final int BATTERY = 2000;

    public static final LngLat START_POSITION = new LngLat(-3.186874, 55.944494);

    //server base address to REST server
    private String baseUrl;

    //year, month, date specify orders on which date will be delivered
    private String year;
    private String month;
    private String date;

    //drone's concrete position on its planned path
    private LngLat currentPosition;

    //drone's remaining battery after finishing delivering an order
    private int remainingBattery;

    //all orders of the given day
    private ArrayList<Order> allOrders;

    //valid order to be delivered with on the given day
    private ArrayList<Order> validOrders;


    //hash map with key being order number of each delivered order,
    //and value being its corresponding shortest path
    private HashMap<String, ArrayList<LngLat>> paths;

    //hash map with key being order number of each delivered order,
    //and value being the direction for each step of the corresponding shortest path
    private HashMap<String, ArrayList<Double>> angles;

    //hash map with key being order number of each delivered order,
    //and value being the computation time of finding each step of the corresponding shortest path
    private HashMap<String, ArrayList<Long>> times;

    //the time when the drone finishes delivering a specific order relative to the start of the entire day delivery
    //after the last order being delivered before the drone exhausts battery,
    //this will denote the total running time for the entire day delivery
    private long totalRunningTime;


    /**
     * Constructor of the drone, which represents its initial condition at the start of the day
     *
     * @param baseUrl URL of the REST base server address
     * @param year    year specified for delivery
     * @param month   month specified for delivery
     * @param date    date specified for delivery
     */
    public Drone(String baseUrl,String year, String month, String date) {
        this.baseUrl = baseUrl;
        this.year = year;
        this.month = month;
        this.date = date;
        this.currentPosition = START_POSITION;
        this.remainingBattery = BATTERY;

        this.paths = new HashMap<>();
        this.angles = new HashMap<>();
        this.times = new HashMap<>();

        //extract all orders of the day using method in Order class
        this.allOrders = Order.getOrdersFromRestServer(baseUrl, year, month, date);


        //select valid orders of the day
        this.validOrders = new ArrayList<>();
        for(Order o: this.allOrders){
            if(o.getOrderStatus() == OrderOutcome.ValidButNotDelivered){
                this.validOrders.add(o);
            }
            else{
                break;
            }
        }


        this.totalRunningTime = 0;

    }

    /**
     * Method for finding the shortest path of all valid orders and other required values
     */
    public void planDailyRoute() {
        for(Order o: this.validOrders){
            String orderNo = o.getOrderNo();
            FlightPath fp = new FlightPath(this.baseUrl,Drone.START_POSITION, o);

            fp.aStarPathFinding();
            ArrayList<LngLat> route = fp.getRoute();
            ArrayList<Double> directions = fp.getAngles();

            //if the drone's battery will not run out before finishing delivering this order,
            //the drone will deliver it
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

            //if the drone's battery will exhaust before finishing delivering this order,
            // it will not deliver it and will stop delivering remaining orders
            else{
                break;
            }
        }
    }

    //getters and setters for private fields of  Drone class
    public HashMap<String, ArrayList<LngLat>> getPaths() {
        return paths;
    }

    public HashMap<String, ArrayList<Double>> getAngles() {
        return angles;
    }

    public HashMap<String, ArrayList<Long>> getTimes() {
        return times;
    }


    public ArrayList<Order> getAllOrders() {
        return allOrders;
    }

    public ArrayList<Order> getValidOrders() {
        return validOrders;
    }



}
