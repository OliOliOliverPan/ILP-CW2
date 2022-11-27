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
            this.deliveryRestaurantList.add(o.findRestaurant().getCoordinate());
        }

    }



    public Stack<LngLat> aStarPathFinding(LngLat destination){
        ArrayList<LngLat> openTable = new ArrayList<>();
        ArrayList<LngLat> closeTable = new ArrayList<>();
        Stack<LngLat> pathStack = new Stack<>();
        this.currentPosition = Drone.START_POSITION;
        this.currentPosition.setPreviousPosition(null);

        LngLat currentPoint = new LngLat(this.currentPosition.getLng(), this.currentPosition.getLat());
        boolean flag = true;


        while(flag){
            for(Direction d: Direction.values()){
                LngLat tempPoint = this.currentPosition.nextPosition(d);
                if(tempPoint.inNoFlyZone(this.noFlyZones)){
                    continue;
                }
                else{
                    if(tempPoint.equals(destination)){
                        flag = false;
                        destination.setPreviousPosition(currentPoint);
                        break;
                    }
                    tempPoint.setG(currentPoint.getG() + currentPoint.distanceTo(tempPoint));
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
        } //end while

        LngLat node = destination;
        while(node.getPreviousPosition() != null){
            pathStack.push(node);
            node = node.getPreviousPosition();
        }

        return pathStack;
    }

    public static void main(String[] args) throws InvalidPizzaCombinationException, MalformedURLException {
        FlightPath fp = new FlightPath("04","15",Drone.START_POSITION, 2000);

        LngLat first_restaurant = fp.deliveryRestaurantList.get(0);
        Stack<LngLat> path = fp.aStarPathFinding(first_restaurant);

        if(path == null){
            System.out.println("No accessible route");
        }else{
            while(! path.isEmpty()){
                System.out.println(path.pop());
            }
            System.out.println();
        }

    }




}
