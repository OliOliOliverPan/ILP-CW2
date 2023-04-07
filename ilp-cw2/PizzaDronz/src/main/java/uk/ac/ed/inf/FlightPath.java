package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.*;

/**
 * Class for implementing drone's shortest path-finding function of an order
 * and recording required values
 */
public class FlightPath {

    //URL of the REST server base address
    private String baseUrl;

    private LngLat currentPosition;

    private NoFlyZone[] noFlyZones;
    private CentralArea centralArea;


    //the order that will be planned with the path
    private Order validOrder;

    //array list recording the route of the order
    private ArrayList<LngLat> route;

    //array list recording the direction of each step of the shortest path
    private ArrayList<Double> angles;

    //array list recording time of outputting each step relative to the start of the route planning
    private ArrayList<Long> eachStepTime;

    //total time cost for computing the entire path of the order
    private long totalTime;


    /**
     * Constructor of the flight path of the order
     *
     * @param baseUrl         URL of the REST base server address
     * @param currentPosition coordinate of drone's current position
     * @param validOrder      Order object to be computed the shortest path with
     */
    public FlightPath(String baseUrl, LngLat currentPosition, Order validOrder) {

        this.currentPosition = currentPosition;
        this.noFlyZones = NoFlyZone.getNoFlyZonesFromRestServer(baseUrl);
        this.centralArea = CentralArea.getInstance();
        this.validOrder = validOrder;

        this.angles = new ArrayList<>();
        this.route = new ArrayList<>();
        this.eachStepTime = new ArrayList<>();
        this.totalTime = 0;

        this.baseUrl = baseUrl;

    }


    /**
     * Method for finding the shortest path of the order and other required values
     */
    public void aStarPathFinding() {
        long tStart = System.currentTimeMillis();

        LngLat destination = this.validOrder.getCorrespondingRestaurantCoordinate();


        //storing corresponding LngLat objects of drone's next position
        //after making a move on one of 16 directions
        ArrayList<LngLat> openTable = new ArrayList<>();

        //storing LngLat objects whose 16 next positions have been already added into openTable
        ArrayList<LngLat> closeTable = new ArrayList<>();

        //stack storing the coordinate of each step of the shortest path
        Stack<LngLat> pathStack = new Stack<>();

        this.currentPosition.setPreviousPosition(null);

        LngLat currentPoint = new LngLat(this.currentPosition.getLng(), this.currentPosition.getLat());


        //final coordinate where the drone arrives at, which is less than 0.00015 degrees to the goal restaurant
        LngLat final_arrived_coordinate = null;

        boolean notNearDestination = true;


        ArrayList<LngLat> noFlyZoneEdgePoints = new ArrayList<>();


        // Extract longitude and latitude data from the object of CentralArea class
        ArrayList<Double> figuresArrayList = this.centralArea.deriveCornerData(this.baseUrl);

        // Coordinates of 4 corners of the central area
        double minimum_lng = figuresArrayList.get(0);
        double maximum_lng = figuresArrayList.get(1);
        double minimum_lat = figuresArrayList.get(2);
        double maximum_lat = figuresArrayList.get(3);


        // Points construction the edges of no-fly-zones
        for(NoFlyZone nfz: this.noFlyZones){
            noFlyZoneEdgePoints.addAll(nfz.getNoFlyZoneEdgePoints());
        }

        while(notNearDestination){
            openTable.clear();

            for(Direction d: Direction.values()){
                LngLat nextPoint = currentPoint.nextPosition(d);

                if(nextPoint.closeToNoFlyZone(noFlyZoneEdgePoints)){
                    continue;
                }

                else{
                    //determine whether the position will make the drone get back into the central area before finishing delivery,
                    //given that the position has been outside central area before making this move
                    LngLat previousPoint = currentPoint.getPreviousPosition();
                    if((previousPoint != null) && (previousPoint.inCentralArea(minimum_lng,minimum_lat,maximum_lng,maximum_lat))
                            && (!currentPoint.inCentralArea(minimum_lng, minimum_lat, maximum_lng, maximum_lat))
                            && (nextPoint.inCentralArea(minimum_lng,minimum_lat,maximum_lng,maximum_lat))){
                        continue;
                    }

                    else{
                        if (nextPoint.closeTo(destination)) {
                            final_arrived_coordinate = nextPoint;
                            notNearDestination = false;
                            final_arrived_coordinate.setPreviousPosition(currentPoint);

                            this.eachStepTime.add(System.currentTimeMillis() - tStart);

                            for(Direction di: Direction.values()){

                                // find the direction of this move
                                if(currentPoint.nextPosition(di).getLng() == nextPoint.getLng() &&
                                        currentPoint.nextPosition(di).getLng() == nextPoint.getLng()){
                                    this.angles.add(di.getAngle());
                                    break;
                                }
                            }
                            break;
                        }
                        nextPoint.setG(currentPoint.getG() + 0.00015);
                        nextPoint.setH(nextPoint.distanceTo(destination));
                        nextPoint.setF(nextPoint.getG() + nextPoint.getH());

                        // if coordinate of drone's next position along a direction is already in closeTable,
                        // determine whether we have found a smaller f value for that coordinate
                        // if so, update its f value and set its previous position as our current position
                        if (closeTable.contains(nextPoint)) {
                            int pos = closeTable.indexOf(nextPoint);
                            LngLat temp = closeTable.get(pos);
                            if (temp.getF() > nextPoint.getF()) {
                                closeTable.remove(pos);
                                openTable.add(nextPoint);
                                nextPoint.setPreviousPosition(currentPoint);
                            }
                        } else {
                            openTable.add(nextPoint);
                            nextPoint.setPreviousPosition(currentPoint);
                        }

                    }
                }

            }

            if(!notNearDestination){
                break; // found the valid path
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

        }

        // we have obtained the shortest path from starting position to a point close to target restaurant,
        // add each position of the path to pathStack
        LngLat node = final_arrived_coordinate;
        while(node.getPreviousPosition() != null){
            pathStack.push(node);
            node = node.getPreviousPosition();
        }
        while(!pathStack.isEmpty()){
            this.route.add(pathStack.pop());
        }

        //number of steps from starting position to the restaurant
        int toSteps = this.route.size();


        //reverse the way to restaurant to obtain the way back to starting position

        //get coordinate for each step of back path
        for(int i = toSteps - 2; i >= 0; i--){
            this.route.add(this.route.get(i));
            this.eachStepTime.add(System.currentTimeMillis() - tStart);
        }
        this.route.add(Drone.START_POSITION);

        //get computation time for each step of back path
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

    //getters and setters for private fields of FlightPath class
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




}