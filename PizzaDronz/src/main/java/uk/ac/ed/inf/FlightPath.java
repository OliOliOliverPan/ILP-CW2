package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

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


        this.deliveryRestaurantList = new ArrayList<>();
        ArrayList<Order> validOrders = Order.getOrdersFromRestServer(month, date);
        for(Order o: validOrders){
            this.deliveryRestaurantList.add(o.getCorrespondingRestaurantCoordinate());
        }

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
        this.currentPosition = Drone.START_POSITION;
        this.currentPosition.setPreviousPosition(null);

        LngLat currentPoint = new LngLat(this.currentPosition.getLng(), this.currentPosition.getLat());
        boolean flag = true;

        //final coordinate where the drone arrives at, which is less than 0.00015 degrees to the goal restaurant
        LngLat final_arrived_coordinate = null;

        while(flag){
            openTable.clear();
            closeTable.clear();


            for(Direction d: Direction.values()){
                LngLat tempPoint = currentPoint.nextPosition(d);
                if(tempPoint.inNoFlyZone()){
                    continue;
                }
              else{
                    if(tempPoint.closeTo(destination)){
                        final_arrived_coordinate = tempPoint;
                        flag = false;
                        final_arrived_coordinate.setPreviousPosition(currentPoint);
                        break;
                    }
                    tempPoint.setG(currentPoint.getG() + 0.00015);
                    tempPoint.setH(tempPoint.distanceTo(destination));
                    tempPoint.setF(tempPoint.getG() + tempPoint.getH());

                    if(openTable.contains(tempPoint)){
                        int pos = openTable.indexOf(tempPoint);
                        LngLat temp = openTable.get(pos);
                        if(temp.getF() > tempPoint.getF()){
                            openTable.remove(pos);
                            openTable.add(tempPoint);
                            tempPoint.setPreviousPosition(currentPoint);
                        }
                    }
                    else if(closeTable.contains(tempPoint)){
                        int pos = closeTable.indexOf(tempPoint);
                        LngLat temp = closeTable.get(pos);
                        if(temp.getF() > tempPoint.getF()){
                            closeTable.remove(pos);
                            openTable.add(tempPoint);
                            tempPoint.setPreviousPosition(currentPoint);
                        }
                    }
                    else{
                        openTable.add(tempPoint);
                        tempPoint.setPreviousPosition(currentPoint);
                    }


                }

            } // end for loop

            if(openTable.isEmpty()){
                return null; // no valid path
            }
            if(flag == false){
                break; // found a valid path
            }
            openTable.remove(currentPoint);
            closeTable.add(currentPoint);
            Collections.sort(openTable);
            currentPoint = openTable.get(0);
            System.out.println("the current chosen point has coordinate" + currentPoint.getLng() + " and "+ currentPoint.getLat());


        } //end while

        LngLat node = final_arrived_coordinate;
        while(node.getPreviousPosition() != null){
            pathStack.push(node);
            node = node.getPreviousPosition();
        }

        return pathStack;
    }

    public static void main(String[] args) throws InvalidPizzaCombinationException, MalformedURLException {
        FlightPath fp = new FlightPath("04","15",Drone.START_POSITION, 2000);

        LngLat first_restaurant_coordinate = new LngLat(-3.202541470527649,55.943284737579376);
        System.out.println("Location of first restaurant:" + first_restaurant_coordinate.getLng() + " and "+first_restaurant_coordinate.getLat());

        Stack<LngLat> path = fp.aStarPathFinding(first_restaurant_coordinate);

        System.out.println("The length of the shortest path to the shortest restaurant is "+ path.size());

        if(path == null){
            System.out.println("No accessible route");
        }else{
            while(! path.isEmpty()){
                LngLat hahaha = path.pop();
                System.out.println(hahaha.getLng() + " and "+hahaha.getLat());
            }
            System.out.println();
        }
        System.out.println(new LngLat(-3.1911798928079014,55.94546984275254).closeTo(new LngLat(-3.1912869215011597,55.945535152517735)));

    }




}
