package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Class defining the no-fly-zone area and its required methods
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoFlyZone {
    /**
     * URL suffix directing to the data for coordinates of no-fly-zone corner points
     */
    public static String noFlyZoneUrl = "noFlyZones";

    //name of no-fly-zone
    private String name;

    //data of no-fly-zone coordinates derived from the server
    private double[][] rawCoordinates;


    //edges of no-fly-zones enclosed by raw coordinates
    private ArrayList<Line2D> noFlyZoneEdges;

    //an array list containing a number of points on no-fly-zone edges after being scaled up,
    //which construct a rough contour of no-fly-zones
    private ArrayList<LngLat> noFlyZoneEdgePoints;


    /**
     * Constructor of a no-fly-zone
     *
     * @param name            name of the no-fly-zone
     * @param rawCoordinates  coordinates of corners of the no-fly-zone derived from REST server
     */
    public NoFlyZone(@JsonProperty("name")String name, @JsonProperty("coordinates")double[][] rawCoordinates){
        this.name = name;
        this.rawCoordinates = rawCoordinates;

        this.noFlyZoneEdges = new ArrayList<>();
        this.noFlyZoneEdgePoints = new ArrayList<>();



        //each no-fly-zone edge is formed by two consecutive raw coordinates using Line2D API,
        //with the first one as the starting point of the edge
        //and the second one as the end point of the edge
        for(int i = 0 ; i < this.rawCoordinates.length - 1; i ++){
            LngLat point = new LngLat(rawCoordinates[i][0], rawCoordinates[i][1]);

            this.noFlyZoneEdges.add(new Line2D.Double(rawCoordinates[i][0], rawCoordinates[i][1], rawCoordinates[i+1][0], rawCoordinates[i+1][1]));
            this.noFlyZoneEdgePoints.add(LngLat.magnify(point));

        }


        for(Line2D edge: this.noFlyZoneEdges) {
            LngLat start = new LngLat(edge.getX1(), edge.getY1());
            LngLat end = new LngLat(edge.getX2(), edge.getY2());

            //find the slope of the edge with respect to positive x-axis
            double angle = Math.atan2(end.getLat() - start.getLat(), end.getLng() - start.getLng());

            int i = 1;

            //On each edge, starting from the starting point,
            //calculate the coordinate of a point every 0.00005 degrees before going over the end point
            //These points will help shape the rough contour of no-fly-zones and replace exact no-fly-zone edges
            //when the drone plans path
            while(i * 0.00005 < start.distanceTo(end)) {
                double tempPointLng = start.getLng() + i * 0.00005 * Math.cos(angle);
                double tempPointLat = start.getLat() + i * 0.00005 * Math.sin(angle);

                //scale up each point
                LngLat magnifiedTempPoint = LngLat.magnify(new LngLat(tempPointLng,tempPointLat));
                this.noFlyZoneEdgePoints.add((magnifiedTempPoint));
                i += 1;
            }

        }

    }






    /**
     * Read the json data from a URL and return an array of corner coordinates of no-fly-zones
     *
     * @param baseUrl the URL of REST base server address
     *
     * @return an array of Restaurant objects including their menus
     */
    public static NoFlyZone[] getNoFlyZonesFromRestServer(String baseUrl){
        if(! baseUrl.endsWith("/")){
            baseUrl += "/";
        }

        NoFlyZone[] noFlyZones = null;

        // Parse the json data
        try {
            noFlyZones = new ObjectMapper().readValue(new URL(baseUrl + noFlyZoneUrl), NoFlyZone[].class);

        } catch (StreamReadException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        return noFlyZones;
    }



    //getter for the private fields of NoFlyZone class
    public ArrayList<LngLat> getNoFlyZoneEdgePoints(){ return this.noFlyZoneEdgePoints;}


}