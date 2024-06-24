package com.bcs05.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void writeToCSVFile() {
        ArrayList<GeoJSONObject> amenityJSON = new ArrayList<>();
        ArrayList<GeoJSONObject> shopJSON = new ArrayList<>();
        ArrayList<GeoJSONObject> tourismJSON = new ArrayList<>();
        try {
            amenityJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(AMENITY_FILE_PATH));
            shopJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(SHOP_FILE_PATH));
            tourismJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(TOURISM_FILE_PATH));

        } catch (Exception e) {
            e.printStackTrace();
        };

        ArrayList<String[]> stuffForFile = new ArrayList<>();

        PostalCodeReader postalReader = new PostalCodeReader();
        HashMap<String, Coordinates> postalCodeMap = postalReader.getRecords();
        Set<String> postalSet = postalCodeMap.keySet();
        ArrayList<String> listOfPostalCodes = new ArrayList<String>(postalSet);
        Collection<Coordinates> coordinateList = postalCodeMap.values();
        ArrayList<Coordinates> listOfCoordinates = new ArrayList<Coordinates>(coordinateList);

        for (int i = 0; i < listOfPostalCodes.size(); i++) {
            BigDecimal postlat = new BigDecimal(listOfCoordinates.get(i).getLatitude());
            BigDecimal postlon = new BigDecimal(listOfCoordinates.get(i).getLongitude());

            Integer amenityCount = 0;
            for (int j = 0; j < amenityJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((amenityJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((amenityJSON.get(j).getCoords()).getLongitude());

                // Checks whether the distance is smaller than or equal to 100 meters
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    amenityCount++;
                }
            }

            Integer shopCount = 0;
            for (int j = 0; j < shopJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((shopJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((shopJSON.get(j).getCoords()).getLongitude());

                // Checks whether the distance is smaller than or equal to 100 meters
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    shopCount++;
                }
            }

            Integer tourismCount = 0;
            for (int j = 0; j < tourismJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((tourismJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((tourismJSON.get(j).getCoords()).getLongitude());

                // Checks whether the distance is smaller than or equal to 100 meters
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    tourismCount++;
                }
            }

            String[] counts = new String[4];
            counts[0] = listOfPostalCodes.get(i);
            counts[1] = String.valueOf(amenityCount);
            counts[2] = String.valueOf(shopCount);
            counts[3] = String.valueOf(tourismCount);
            stuffForFile.add(counts);
        }
        
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/countofammenities.txt");
            myWriter.write("Postal Code, Amenity Count, Shop Count, Tourism Count\n");
            for (int i = 0; i < stuffForFile.size(); i++) {
                myWriter.write(stuffForFile.get(i)[0] + ", " + stuffForFile.get(i)[1] + ", " + stuffForFile.get(i)[2] + ", " + stuffForFile.get(i)[3] + "\n");
            }
            System.out.println("Successfully wrote to the file.");
            myWriter.close();

            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

    }

}