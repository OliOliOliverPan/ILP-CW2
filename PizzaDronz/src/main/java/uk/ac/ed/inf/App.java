package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.LineString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws IOException {

        Drone drone = new Drone("04","15");
        drone.planDailyRoute();

        List<JsonOrder> deliveryResult = new ArrayList<>();
        for(Order o: drone.getValidOrders()){
            deliveryResult.add(new JsonOrder(o.getOrderNo(),o.getOrderStatus(),o.getPriceTotalInPence()));
        }

        List<JsonRoute> pathsResult = new ArrayList<>();

        List<Point> flightPathPoints = new ArrayList<>();


        for(Order o:drone.getValidOrders()){
            if(o.getOrderStatus() == OrderOutcome.Delivered){
                ArrayList<LngLat> route = drone.getPaths().get(o.getOrderNo());
                ArrayList<Double> angles = drone.getAngles().get(o.getOrderNo());
                ArrayList<Long> times = drone.getTimes().get(o.getOrderNo());

                //adding all steps of route to the restaurant
                pathsResult.add(new JsonRoute(o.getOrderNo(), Drone.START_POSITION.getLng(),Drone.START_POSITION.getLat(), angles.get(0), route.get(0).getLng(),route.get(0).getLat(), times.get(0)));

                flightPathPoints.add(Point.fromLngLat(Drone.START_POSITION.getLng(), Drone.START_POSITION.getLat()));


                for(int i = 0; i < route.size() - 1; i++){
                    pathsResult.add(new JsonRoute(o.getOrderNo(), route.get(i).getLng(),route.get(i).getLat(), angles.get(i+1), route.get(i+1).getLng(),route.get(i+1).getLat(), times.get(i+1)));

                    flightPathPoints.add(Point.fromLngLat(route.get(i).getLng(), route.get(i).getLat()));
                }

                flightPathPoints.add(Point.fromLngLat(route.get(route.size()-1).getLng(), route.get(route.size()-1).getLat()));



            }
            else{
                break;
            }
        }

        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter().writeValue(new File("deliveries-2023-04-15.json"), deliveryResult);

        om.writerWithDefaultPrettyPrinter().writeValue(new File("flightpath-2023-04-15.json"), pathsResult);



        LineString flightpathLineString = LineString.fromLngLats(flightPathPoints);
        Feature flightpathFeature = Feature.fromGeometry( flightpathLineString );

        // convert flightpath to one feature in a feature collection
        ArrayList<Feature> flightpathList = new ArrayList<Feature>();
        flightpathList.add(flightpathFeature);
        FeatureCollection flightpathFC = FeatureCollection.fromFeatures(flightpathList);
        String flightpathJson = flightpathFC.toJson();

        // write the geojson file
        try {
            FileWriter myWriter = new FileWriter(
                    "drone-2023-04-15" + ".geojson");
            myWriter.write(flightpathJson);
            myWriter.close();

        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate flightpath Geojson");
            e.printStackTrace();
        }

        System.out.println(drone.getRemainingBattery());
    }




}