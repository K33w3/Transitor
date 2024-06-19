package com.bcs05.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.math.BigDecimal;
import java.util.Collection;

import com.bcs05.data.GeoJSONReader;
import com.bcs05.data.PostalCodeReader;
import com.bcs05.engine.DistanceCalculator;

public class JSONAccessabilityScores {
    private static String AMENITY_FILE_PATH = "src/main/resources/amenity.geojson";
    private static String SHOP_FILE_PATH = "src/main/resources/shop.geojson";
    private static String TOURISM_FILE_PATH = "src/main/resources/tourism.geojson";

    public static ArrayList<GeoJSONObject> createJSONList() {
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
        return parsedJSON;
    }

    public static HashMap<String, Integer> createAccessabilityScoreList() {
        ArrayList<GeoJSONObject> parsedJSON = createJSONList();
        HashMap<String, Integer> accessabilityScoresMap = new HashMap<>();

        PostalCodeReader postalReader = new PostalCodeReader();
        HashMap<String, Coordinates> postalCodeMap = postalReader.getRecords();
        Set<String> postalSet = postalCodeMap.keySet();
        ArrayList<String> listOfPostalCodes = new ArrayList<String>(postalSet);
        Collection<Coordinates> coordinateList = postalCodeMap.values();
        ArrayList<Coordinates> listOfCoordinates = new ArrayList<Coordinates>(coordinateList);

        int accessabilityScore = 0;
        for (int i = 0; i < listOfPostalCodes.size(); i++) {
            accessabilityScore = 0;
            BigDecimal postlat = new BigDecimal(listOfCoordinates.get(i).getLatitude());
            BigDecimal postlon = new BigDecimal(listOfCoordinates.get(i).getLongitude());

            for (int j = 0; j < parsedJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((parsedJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((parsedJSON.get(j).getCoords()).getLongitude());

                // Checks whether the distance is smaller than or equal to 100 meters
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(0.1)) <= 0) {
                    accessabilityScore++;
                }
            }

            accessabilityScoresMap.put(listOfPostalCodes.get(i), accessabilityScore);
        }
        return accessabilityScoresMap;
    }

}