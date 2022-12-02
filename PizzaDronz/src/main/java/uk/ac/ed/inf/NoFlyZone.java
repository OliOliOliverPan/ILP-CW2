package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NoFlyZone {
    private static final Restaurant[] INSTANCE = null;
    public static String noFlyZoneUrl = "https://ilp-rest.azurewebsites.net/noFlyZones";
    public static NoFlyZone[] getINSTANCE() throws MalformedURLException {  return getNoFlyZonesFromRestServer(new URL(noFlyZoneUrl)); }

    private String name;

    private double[][] rawCoordinates;

    private Path2D noFlyZoneArea = new Path2D.Double();

    private  ArrayList<LngLat> noFlyZoneCoordinates;

    private ArrayList<Line2D> noFlyZoneEdges;

    private ArrayList<LngLat> noFlyZoneEdgePoints;

    public NoFlyZone(@JsonProperty("name")String name, @JsonProperty("coordinates")double[][] rawCoordinates){
        this.name = name;
        this.rawCoordinates = rawCoordinates;
        this.noFlyZoneCoordinates = new ArrayList<>();
        this.noFlyZoneEdges = new ArrayList<>();
        this.noFlyZoneEdgePoints = new ArrayList<>();


//        this.noFlyZoneArea.moveTo(this.rawCoordinates[0][0], this.rawCoordinates[0][1]);
//        for(int i = 1 ; i < this.rawCoordinates.length - 1; i ++) {
//            this.noFlyZoneArea.lineTo(this.rawCoordinates[i][0], this.rawCoordinates[i][1]);
//        }
//        this.noFlyZoneArea.closePath();

        for(int i = 0 ; i < this.rawCoordinates.length - 1; i ++){
            LngLat point = new LngLat(rawCoordinates[i][0], rawCoordinates[i][1]);
            this.noFlyZoneCoordinates.add(point);
            this.noFlyZoneEdges.add(new Line2D.Double(rawCoordinates[i][0], rawCoordinates[i][1], rawCoordinates[i+1][0], rawCoordinates[i+1][1]));
            this.noFlyZoneEdgePoints.add(LngLat.magnify(point));

        }


        for(Line2D edge: this.noFlyZoneEdges) {
            LngLat start = new LngLat(edge.getX1(), edge.getY1());
            LngLat end = new LngLat(edge.getX2(), edge.getY2());
            double angle = Math.atan2(end.getLat() - start.getLat(), end.getLng() - start.getLng());

            int i = 1;

            while(i * 0.00005 < start.distanceTo(end)) {
                double tempPointLng = start.getLng() + i * 0.00005 * Math.cos(angle);
                double tempPointLat = start.getLat() + i * 0.00005 * Math.sin(angle);
                LngLat magnifiedTempPoint = LngLat.magnify(new LngLat(tempPointLng,tempPointLat));
                this.noFlyZoneEdgePoints.add((magnifiedTempPoint));
                i += 1;
            }

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

    public ArrayList<LngLat> getNoFlyZoneCoordinates() { return this.noFlyZoneCoordinates; }

    public ArrayList<LngLat> getNoFlyZoneEdgePoints(){ return this.noFlyZoneEdgePoints;}

    public Path2D getNoFlyZoneArea() {
        return noFlyZoneArea;
    }

    public static void main(String[] args) throws MalformedURLException {
        System.out.println(Math.atan2(55.94284650540911 - 55.94402412577528, -3.1899887323379517 + 3.190578818321228));


        NoFlyZone[] nfzs = NoFlyZone.getINSTANCE();



//        for(int j = 0; j < nfzs.length; j ++) {
//            NoFlyZone first_nfz = nfzs[j];
//            System.out.println(first_nfz.name);
//
//            for (int i = 0; i < first_nfz.getNoFlyZoneCoordinates().size(); i++) {
//                System.out.println(first_nfz.getNoFlyZoneCoordinates().get(i).getLng());
//                System.out.println(first_nfz.getNoFlyZoneCoordinates().get(i).getLat());
//            }
//
//            System.out.println();
//        }


        for(NoFlyZone n: nfzs){
            for(LngLat temp: n.noFlyZoneEdgePoints){
                System.out.println(temp.getLng());
                System.out.println(temp.getLat());
            }
        }

        //System.out.println(count);



    }

}