package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.*;

public class FlightPath {

    private LngLat currentPosition;

    private NoFlyZone[] noFlyZones;
    private CentralArea centralArea;


    private Order validOrder;

    private ArrayList<LngLat> route;
    private ArrayList<Double> angles;

    //time of making a step since the route planning of the order starts
    private ArrayList<Long> eachStepTime;

    private long totalTime;

    public FlightPath( LngLat currentPosition, Order validOrder) throws MalformedURLException {

        this.currentPosition = currentPosition;
        this.noFlyZones = NoFlyZone.getINSTANCE();
        this.centralArea = CentralArea.getInstance();
        this.validOrder = validOrder;

        this.angles = new ArrayList<>();
        this.route = new ArrayList<>();
        this.eachStepTime = new ArrayList<>();
        this.totalTime = 0;

    }



    public void aStarPathFinding(Order validOrder) throws MalformedURLException {
        long tStart = System.currentTimeMillis();

        LngLat destination = validOrder.getCorrespondingRestaurantCoordinate();


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
            //closeTable.clear();


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

                            this.eachStepTime.add(System.currentTimeMillis() - tStart);

                            for(Direction di: Direction.values()){
                                if(currentPoint.nextPosition(di).getLng() == tempPoint.getLng() &&
                                        currentPoint.nextPosition(di).getLng() == tempPoint.getLng()){
                                    this.angles.add(di.getAngle());
                                    break;
                                }
                            }
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

            this.eachStepTime.add(System.currentTimeMillis() - tStart);

            for(Direction di: Direction.values()){
                if(currentPoint.getPreviousPosition().nextPosition(di).getLng() == currentPoint.getLng() &&
                        currentPoint.getPreviousPosition().nextPosition(di).getLng() == currentPoint.getLng()){
                    this.angles.add(di.getAngle());
                    break;
                }
            }

        } //end while

        LngLat node = final_arrived_coordinate;
        while(node.getPreviousPosition() != null){
            pathStack.push(node);
            node = node.getPreviousPosition();
        }

        while(!pathStack.isEmpty()){
            this.route.add(pathStack.pop());
        }

        //number of steps to go to the restaurant
        int toSteps = this.route.size();


        //reverse the way to restaurant to obtain the way back to Appleton Tower

        //get coordinate for each back step
        for(int i = toSteps - 2; i >= 0; i--){
            this.route.add(this.route.get(i));
            this.eachStepTime.add(System.currentTimeMillis() - tStart);
        }
        this.route.add(Drone.START_POSITION);
        this.eachStepTime.add(System.currentTimeMillis() - tStart);


        //get directions for each back step
        for(int i = 0; i < toSteps; i ++){
            Double angle = this.angles.get(i);

            //get the reversed direction
            this.angles.add((angle + 180) % 360);

        }


        long tEnd = System.currentTimeMillis();

        this.totalTime = tEnd - tStart;

    }

    public ArrayList<LngLat> getRoute() {
        return route;
    }

    public ArrayList<Double> getAngles() {
        return angles;
    }

    public ArrayList<Long> getEachStepTime() {
        return eachStepTime;
    }

    public long getTotalTime() {
        return totalTime;
    }




//planning the route for all orders of the day
//    public ArrayList<LngLat> planDailyRoute() throws MalformedURLException {
//        boolean enoughBattery = true;
//
//        ArrayList<LngLat> paths = new ArrayList<>();
//
//        //records each restaurant and corresponding shortest path from Appleton Tower to it
//        HashMap<String, ArrayList<LngLat>> restaurantPath = new HashMap<>();
//
//        for(Order o: this.validOrders){
//            boolean foundInMap = false;
//            Restaurant r = o.getCorrespondingRestaurant();
//
//            //records the round trip path for every single order
//            ArrayList<LngLat> wholePath = new ArrayList<>();
//
//            for(String rName: restaurantPath.keySet()) {
//                if (rName.equals(r.getName())) {
//                    wholePath = restaurantPath.get(rName);
//                    foundInMap = true;
////
//                }
//            }
//
//            if(!foundInMap) {
//                Stack<LngLat> pathToRestaurant = this.aStarPathFinding(r.getCoordinate());
//                Collections.reverse(pathToRestaurant);
//                wholePath.addAll(pathToRestaurant);
//                pathToRestaurant.pop();
//                Collections.reverse(pathToRestaurant);
//                wholePath.addAll(pathToRestaurant);
//                wholePath.add(Drone.START_POSITION);
//
//                restaurantPath.put(r.getName(), wholePath);
//            }
//
//            if(wholePath.size() + 2 <= this.remainingBattery){
//
//                paths.addAll(wholePath);
//
//                this.remainingBattery = this.remainingBattery - wholePath.size() - 2;
//                o.setOrderStatus(OrderOutcome.Delivered);
//
//            }
//            else{
//                enoughBattery = false;
//            }
//
//            if(!enoughBattery){
//                break;
//            }
//        }
//
//        return paths;
//    }





    public static void main(String[] args) throws MalformedURLException {

        ArrayList<Order> validOrders = Order.getOrdersFromRestServer("04","15");
        Order first_order = validOrders.get(20);
        FlightPath fp = new FlightPath(Drone.START_POSITION, first_order);



//        for(Order o: fp.validOrders){
//            System.out.println(o.getOrderStatus());
//        }
        fp.aStarPathFinding(first_order);
        System.out.println(fp.route.size());
        for(LngLat l: fp.route){
            System.out.println(l.getLng());
            System.out.println(l.getLat());
        }
        for(Double i: fp.angles){
            System.out.println(i);
        }

        System.out.println(fp.route.get(fp.route.size() / 2 -1).getLng());
        System.out.println(fp.route.get(fp.route.size() / 2 -1).getLat());
        System.out.println(new LngLat(-3.193964,55.943924).closeTo(new LngLat(-3.1940174102783203,55.94390696616939)));
//        System.out.println(fp.totalTime);
//        for(long i:fp.eachStepTime){
//            System.out.println(i);
//        }


//        while(!pathToRestaurant.isEmpty()){
//            LngLat l = pathToRestaurant.pop();
//            System.out.println(l.getLng());
//            System.out.println(l.getLat());
//        }

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