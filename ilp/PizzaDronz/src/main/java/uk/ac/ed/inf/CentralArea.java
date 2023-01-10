package uk.ac.ed.inf;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Singleton class whose only object stores the longitude and latitude data of central area corner points
 */

public class CentralArea {
    private static final CentralArea INSTANCE = new CentralArea();


    /**
     * URL suffix directing to the data for coordinates of central area corner points
     */
    private static String locationUrl = "centralarea";

    public static CentralArea getInstance(){
        return INSTANCE;
    }


    /**
     * Obtain coordinates of the central area corners
     *
     * @return an arraylist consisting of the longitude and latitude data of central area corner points
     *
     */
    public static ArrayList<Double> deriveCornerData(String baseUrl){
        if(! baseUrl.endsWith("/")){
            baseUrl += "/";
        }

        LngLat[] locationsList = null;

        // Read values of longitude and latitude of 4 corner points of the central area from URL using jackson
        try{
            locationsList = new ObjectMapper().readValue(new URL(baseUrl+locationUrl), LngLat[].class);

        } catch (StreamReadException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        // For those coordinate values,
        // extract unique values and sort them using tree set.
        // Based on the location of Edinburgh,
        // we can determine that the first two values represent the longitudinal range of central area,
        // and the last two values represent the latitudinal range of central area
        TreeSet<Double> figures = new TreeSet<>();

        for(LngLat location: locationsList){
            figures.add(location.getLat());
            figures.add(location.getLng());
        }

        // Transfer values in the tree set to an arraylist,
        // since its get() method makes getting the value with given index more convenient
        ArrayList<Double> figuresArrayList = new ArrayList<>();
        figuresArrayList.addAll(figures);

        return figuresArrayList;
    }


}
