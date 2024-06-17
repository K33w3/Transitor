package com.bcs05.util;

import java.util.ArrayList;
import com.bcs05.data.GeoJSONReader;

public class jsonCoordinateArray {
    private static String AMENITY_FILE_PATH = "src/main/resources/amenity.geojson";
    private static String SHOP_FILE_PATH = "src/main/resources/shop.geojson";
    private static String TOURISM_FILE_PATH = "src/main/resources/tourism.geojson";

    public static ArrayList<ArrayList<String>> createCoordinateArray() {
        ArrayList<GeoJSONObject> parsedJSON = new ArrayList<>();
        try {
            ArrayList<GeoJSONObject> amenityJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(AMENITY_FILE_PATH));
            ArrayList<GeoJSONObject> shopJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(SHOP_FILE_PATH));
            ArrayList<GeoJSONObject> tourismJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(TOURISM_FILE_PATH));
            parsedJSON.addAll(amenityJSON);
            parsedJSON.addAll(shopJSON);
            parsedJSON.addAll(tourismJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<String>> coordList = new ArrayList<>();
        for (int i = 0; i < parsedJSON.size(); i++) {
            String lat = (parsedJSON.get(i).getCoords()).getLatitude();
            String lon = (parsedJSON.get(i).getCoords()).getLongitude();
            String type = parsedJSON.get(i).getType();
            ArrayList<String> typeCoords = new ArrayList<>();
            typeCoords.add(type);
            typeCoords.add(lat);
            typeCoords.add(lon);
            coordList.add(typeCoords);
        }
        return coordList;
    }

}