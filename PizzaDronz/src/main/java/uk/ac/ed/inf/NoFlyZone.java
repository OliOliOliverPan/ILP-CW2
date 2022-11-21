package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NoFlyZone {
    public static String noFlyZoneUrl = "https://ilp-rest.azurewebsites.net/noFlyZones";

    private String name;

    private double[][] rawCoordinates;

    private Path2D noFlyZoneArea = new Path2D.Double();

    private  ArrayList<LngLat> noFlyZoneCoordinates = new ArrayList<>();


    public NoFlyZone(@JsonProperty("name")String name, @JsonProperty("coordinates")double[][] rawCoordinates){
        this.name = name;
        this.rawCoordinates = rawCoordinates;

        this.noFlyZoneArea.moveTo(this.rawCoordinates[0][0], this.rawCoordinates[0][1]);
        for(int i = 1 ; i < this.rawCoordinates.length - 1; i ++) {
            this.noFlyZoneArea.lineTo(this.rawCoordinates[i][0], this.rawCoordinates[i][1]);
        }
        this.noFlyZoneArea.closePath();

        for(int i = 0 ; i < this.rawCoordinates.length; i ++){
            this.noFlyZoneCoordinates.add(new LngLat(rawCoordinates[i][0], rawCoordinates[i][1]));


        }
    }






    public static NoFlyZone[] getNoFlyZonesFromRestServer(URL serverBaseAddress){
        NoFlyZone[] noFlyZones = null;

        // Parse the JSON data
        try {
            noFlyZones = new ObjectMapper().readValue(serverBaseAddress, NoFlyZone[].class);

        } catch (StreamReadException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        return noFlyZones;
    }




    public double[][] getRawCoordinates(){
        return this.rawCoordinates;
    }

    public List<LngLat> getNoFlyZoneCoordinates() { return this.noFlyZoneCoordinates; }


    public Path2D getNoFlyZoneArea() {
        return this.noFlyZoneArea;
    }

    public static void main(String[] args) throws MalformedURLException {
        NoFlyZone[] nfzs = getNoFlyZonesFromRestServer(new URL(NoFlyZone.noFlyZoneUrl));



        for(int j = 0; j < nfzs.length; j ++) {
            NoFlyZone first_nfz = nfzs[j];
            System.out.println(first_nfz.name);

            for (int i = 0; i < first_nfz.getNoFlyZoneCoordinates().size(); i++) {
                System.out.println(first_nfz.getNoFlyZoneCoordinates().get(i).getLng());
                System.out.println(first_nfz.getNoFlyZoneCoordinates().get(i).getLat());
            }

            System.out.println();
        }




    }

}
