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
 * Class for running the entire application
 *
 */
public class App
{

    /**
     * Main method of the class
     *
     * args[0]  the time of the delivery
     * args[1]  server base URL
     *
     * @throws IOException if the drone fails to extract data from the provided URL
     */
    public static void main( String[] args ) throws IOException {


        //get delivery time and server base address from command line
        String[] yearMonthDate = args[0].split("-");
        String year = yearMonthDate[0].substring(2,4); // only store the last two digits of the year
        String month = yearMonthDate[1];
        String date = yearMonthDate[2];

        //for month and date, if they are less than 10 and do not start with 0, a 0 will be automatically added before them
        if(month.length() == 1){
            month = "0" + month;
        }

        if(date.length() == 1){
            date = "0" + date;
        }

        String baseUrl = args[1];

        Drone drone = new Drone(baseUrl,year,month,date);
        drone.planDailyRoute();

        //store results for writing deliveries json file
        List<JsonOrder> deliveryResult = new ArrayList<>();
        for(Order o: drone.getAllOrders()){
            deliveryResult.add(new JsonOrder(o.getOrderNo(),o.getOrderStatus(),o.getPriceTotalInPence()));
        }

        List<JsonRoute> pathsResult = new ArrayList<>();

        List<Point> flightPathPoints = new ArrayList<>();


        //store results for writing flightPath json file
        for(Order o:drone.getValidOrders()){
            if(o.getOrderStatus() == OrderOutcome.Delivered){
                ArrayList<LngLat> route = drone.getPaths().get(o.getOrderNo());
                ArrayList<Double> angles = drone.getAngles().get(o.getOrderNo());
                ArrayList<Long> times = drone.getTimes().get(o.getOrderNo());

                //add the starting position of the day to the entire route list
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

        //write two json files using jackson APIs
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter().writeValue(new File("deliveries"+"-"+"20"+year+"-"+month+"-" + date+".json"), deliveryResult);

        om.writerWithDefaultPrettyPrinter().writeValue(new File("flightpath"+"-"+"20"+year+"-"+month+"-" + date+".json"), pathsResult);



        //write geojson file using com.mapbox.geojson APIs
        LineString flightpathLineString = LineString.fromLngLats(flightPathPoints);
        Feature flightPathFeature = Feature.fromGeometry( flightpathLineString );

        //convert flightpath to one feature in a feature collection
        ArrayList<Feature> flightPathList = new ArrayList<>();
        flightPathList.add(flightPathFeature);
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(flightPathList);
        String flightPathJson = featureCollection.toJson();

        // write geojson file
        try {
            FileWriter myWriter = new FileWriter(
                    "drone"+"-"+"20"+year+"-"+month+"-"+date + ".geojson");
            myWriter.write(flightPathJson);
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
