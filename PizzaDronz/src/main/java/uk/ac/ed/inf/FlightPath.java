package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.*;

public class FlightPath {

    private String month;
    private String date;
    private LngLat currentPosition;
    private int remainingBattery;

    private NoFlyZone[] noFlyZones;
    private CentralArea centralArea;

    private ArrayList<Order> validOrders;

    public FlightPath(String month, String date, LngLat currentPosition, int remainingBattery) throws MalformedURLException, InvalidPizzaCombinationException {
        this.month = month;
        this.date = date;
        this.currentPosition = currentPosition;
        this.remainingBattery = remainingBattery;

        this.noFlyZones = NoFlyZone.getINSTANCE();
        this.centralArea = CentralArea.getInstance();

        this.validOrders = Order.getOrdersFromRestServer(month, date);

    }

//    public FlightPath(String month, String date, LngLat currentPosition, int remainingBattery){
//        this.month = month;
//        this.date = date;
//        this.currentPosition = currentPosition;
//        this.remainingBattery = remainingBattery;
//    }




    public Stack<LngLat> aStarPathFinding(LngLat destination) throws MalformedURLException {
        ArrayList<LngLat> openTable = new ArrayList<>();
        ArrayList<LngLat> closeTable = new ArrayList<>();
        Stack<LngLat> pathStack = new Stack<>();

        this.currentPosition.setPreviousPosition(null);

        LngLat currentPoint = new LngLat(this.currentPosition.getLng(), this.currentPosition.getLat());
        boolean flag = true;

        //final coordinate where the drone arrives at, which is less than 0.00015 degrees to the goal restaurant
        LngLat final_arrived_coordinate = null;


        ArrayList<LngLat> noFlyZoneEdgePoints = new ArrayList<>();


        // Extract longitude and latitude data from the object of CentralArea class
        ArrayList<Double> figuresArrayList = this.centralArea.deriveCornerData();

        double minimum_lng = figuresArrayList.get(0);
        double maximum_lng = figuresArrayList.get(1);
        double minimum_lat = figuresArrayList.get(2);
        double maximum_lat = figuresArrayList.get(3);


        for(NoFlyZone nfz: this.noFlyZones){
            noFlyZoneEdgePoints.addAll(nfz.getNoFlyZoneEdgePoints());
        }

        while(flag){
            openTable.clear();
            closeTable.clear();


            for(Direction d: Direction.values()){
                LngLat tempPoint = currentPoint.nextPosition(d);
                if(tempPoint.closeToNoFlyZone(noFlyZoneEdgePoints)){
                    continue;
                }
                else{
                    LngLat previousPoint = currentPoint.getPreviousPosition();
                    if((previousPoint != null) && (previousPoint.inCentralArea(minimum_lng,minimum_lat,maximum_lng,maximum_lat))
                            && (!currentPoint.inCentralArea(minimum_lng, minimum_lat, maximum_lng, maximum_lat))
                            && (tempPoint.inCentralArea(minimum_lng,minimum_lat,maximum_lng,maximum_lat))){
                        continue;
                    }

                    else{
                        if (tempPoint.closeTo(destination)) {
                            final_arrived_coordinate = tempPoint;
                            flag = false;
                            final_arrived_coordinate.setPreviousPosition(currentPoint);
                            break;
                        }
                        tempPoint.setG(currentPoint.getG() + 0.00015);
                        tempPoint.setH(tempPoint.distanceTo(destination));
                        tempPoint.setF(tempPoint.getG() + tempPoint.getH());

                        if (openTable.contains(tempPoint)) {
                            int pos = openTable.indexOf(tempPoint);
                            LngLat temp = openTable.get(pos);
                            if (temp.getF() > tempPoint.getF()) {
                                openTable.remove(pos);
                                openTable.add(tempPoint);
                                tempPoint.setPreviousPosition(currentPoint);
                            }
                        } else if (closeTable.contains(tempPoint)) {
                            int pos = closeTable.indexOf(tempPoint);
                            LngLat temp = closeTable.get(pos);
                            if (temp.getF() > tempPoint.getF()) {
                                closeTable.remove(pos);
                                openTable.add(tempPoint);
                                tempPoint.setPreviousPosition(currentPoint);
                            }
                        } else {
                            openTable.add(tempPoint);
                            tempPoint.setPreviousPosition(currentPoint);
                        }

                    }
                }

            } // end for loop

//            if(openTable.isEmpty()){
//                return null; // no valid path
//            }
            if(!flag){
                break; // found a valid path
            }
            openTable.remove(currentPoint);
            closeTable.add(currentPoint);
            Collections.sort(openTable);
            currentPoint = openTable.get(0);
            //System.out.println("the current chosen point has coordinate" + currentPoint.getLng() + " and "+ currentPoint.getLat());


        } //end while

        LngLat node = final_arrived_coordinate;
        while(node.getPreviousPosition() != null){
            pathStack.push(node);
            node = node.getPreviousPosition();
        }

        return pathStack;
    }


    //planning the route for all orders of the day
    public ArrayList<LngLat> planDailyRoute() throws MalformedURLException {
        boolean enoughBattery = true;

        ArrayList<LngLat> paths = new ArrayList<>();

        //records each restaurant and corresponding shortest path from Appleton Tower to it
        HashMap<String, ArrayList<LngLat>> restaurantPath = new HashMap<>();

        for(Order o: this.validOrders){
            boolean foundInMap = false;
            Restaurant r = o.getCorrespondingRestaurant();

            //records the round trip path for every single order
            ArrayList<LngLat> wholePath = new ArrayList<>();

            for(String rName: restaurantPath.keySet()) {
                if (rName.equals(r.getName())) {
                    wholePath = restaurantPath.get(rName);
                    foundInMap = true;
//
                }
            }

            if(!foundInMap) {
                Stack<LngLat> pathToRestaurant = this.aStarPathFinding(r.getCoordinate());
                Collections.reverse(pathToRestaurant);
                wholePath.addAll(pathToRestaurant);
                pathToRestaurant.pop();
                Collections.reverse(pathToRestaurant);
                wholePath.addAll(pathToRestaurant);
                wholePath.add(Drone.START_POSITION);

                restaurantPath.put(r.getName(), wholePath);
            }

            if(wholePath.size() + 2 <= this.remainingBattery){

                paths.addAll(wholePath);

                this.remainingBattery = this.remainingBattery - wholePath.size() - 2;
                o.setOrderStatus(OrderOutcome.Delivered);

            }
            else{
                enoughBattery = false;
            }

            if(!enoughBattery){
                break;
            }
        }

        return paths;
    }





    public static void main(String[] args) throws InvalidPizzaCombinationException, MalformedURLException {

        FlightPath fp = new FlightPath("04","15",Drone.START_POSITION, 2000);

        ArrayList<LngLat> dailyPath = fp.planDailyRoute();
        System.out.println(dailyPath.size());
        System.out.println(fp.remainingBattery);
        for(Order o: fp.validOrders){
            System.out.println(o.getOrderStatus());
        }
//        Stack<LngLat> pathToRestaurant = fp.aStarPathFinding(new LngLat(-3.202541470527649,55.943284737579376));
//        //System.out.println(pathToRestaurant.size());
//
//        ArrayList<LngLat> wholePath = new ArrayList<>();
//
//        Collections.reverse(pathToRestaurant);
//        wholePath.addAll(pathToRestaurant);
//        pathToRestaurant.pop();
//        Collections.reverse(pathToRestaurant);
//        wholePath.addAll(pathToRestaurant);
//        wholePath.add(Drone.START_POSITION);
//
//        for(LngLat a: wholePath){
//            System.out.println(a.getLng());
//        }
//        System.out.println(wholePath.size());




    }




}