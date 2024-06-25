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

/**
 * This class calculates accessibility scores based on the proximity of postal codes to different types of 
 * ammenities, shops and tourism spots using GeoJSON data.
 */
public class JSONAccessabilityScores {

    // File paths for different types of GeoJSON data
    private static String AMENITY_FILE_PATH = "src/main/resources/amenity.geojson";
    private static String SHOP_FILE_PATH = "src/main/resources/shop.geojson";
    private static String TOURISM_FILE_PATH = "src/main/resources/tourism.geojson";

    /**
     * Creates a combines list of GeoJSON objects from amenity, shop and tourism files
     * 
     * @return an ArrayList of GeoJSONObject containing parsed GeoJSON data from all sources
     */
    public static ArrayList<GeoJSONObject> createJSONList() {
        ArrayList<GeoJSONObject> parsedJSON = new ArrayList<>();
        try {
            // Parse GeoJSON data from the files
            ArrayList<GeoJSONObject> amenityJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(AMENITY_FILE_PATH));
            ArrayList<GeoJSONObject> shopJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(SHOP_FILE_PATH));
            ArrayList<GeoJSONObject> tourismJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(TOURISM_FILE_PATH));

            // Combine all parsed JSON data into one list
            parsedJSON.addAll(amenityJSON);
            parsedJSON.addAll(shopJSON);    
            parsedJSON.addAll(tourismJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedJSON;
    }

    /**
     * Calculates accessibility scores for each postal code based on its proximity to amenities, shops and tourism spots.
     * 
     * @return a hashmap with postal codes as keys and their accessibility scores as values
     */
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

                // Checks whether the distance is smaller than or equal to 1.2 kilometers
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    accessabilityScore++;
                }
            }

            // Store accesibility score for each postal code in the map
            accessabilityScoresMap.put(listOfPostalCodes.get(i), accessabilityScore);
        }
        return accessabilityScoresMap;
    }

    /**
     * Writes accesibility scores to a CSV file, including counts of amenities, 
     * shops and tourism spots for each postal code
     */
    public static void writeToCSVFile() {
        ArrayList<GeoJSONObject> amenityJSON = new ArrayList<>();
        ArrayList<GeoJSONObject> shopJSON = new ArrayList<>();
        ArrayList<GeoJSONObject> tourismJSON = new ArrayList<>();
        try {
            // parse GeoJSON data from files
            amenityJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(AMENITY_FILE_PATH));
            shopJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(SHOP_FILE_PATH));
            tourismJSON = GeoJSONParser.parseGeoJSON(GeoJSONReader.readFile(TOURISM_FILE_PATH));

        } catch (Exception e) {
            e.printStackTrace();
        };

        ArrayList<String[]> stuffForFile = new ArrayList<>();

        // Read postal code data
        PostalCodeReader postalReader = new PostalCodeReader();
        HashMap<String, Coordinates> postalCodeMap = postalReader.getRecords();
        Set<String> postalSet = postalCodeMap.keySet();
        ArrayList<String> listOfPostalCodes = new ArrayList<String>(postalSet);
        Collection<Coordinates> coordinateList = postalCodeMap.values();
        ArrayList<Coordinates> listOfCoordinates = new ArrayList<Coordinates>(coordinateList);

        // Calculate the count of amenities, shops and tourism spots for each postal code
        for (int i = 0; i < listOfPostalCodes.size(); i++) {
            BigDecimal postlat = new BigDecimal(listOfCoordinates.get(i).getLatitude());
            BigDecimal postlon = new BigDecimal(listOfCoordinates.get(i).getLongitude());

            // Calculate amenity count
            Integer amenityCount = 0;
            for (int j = 0; j < amenityJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((amenityJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((amenityJSON.get(j).getCoords()).getLongitude());

                // Check whether the distance is smaller than or equal to 1.2 kilometers
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    amenityCount++;
                }
            }

            // Calculate shop count
            Integer shopCount = 0;
            for (int j = 0; j < shopJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((shopJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((shopJSON.get(j).getCoords()).getLongitude());

                // Check whether the distance is smaller than or equal to 1.2 kilometers
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    shopCount++;
                }
            }

            // Calculate tourism spots count
            Integer tourismCount = 0;
            for (int j = 0; j < tourismJSON.size(); j++) {
                BigDecimal lat = new BigDecimal((tourismJSON.get(j).getCoords()).getLatitude());
                BigDecimal lon = new BigDecimal((tourismJSON.get(j).getCoords()).getLongitude());

                // Check whether the distance is smaller than or equal to 1.2 kilometers
                if (DistanceCalculator.calculateAerialDistance(postlat, postlon, lat, lon).compareTo(BigDecimal.valueOf(1.2)) <= 0) {
                    tourismCount++;
                }
            }

            // Store counts in an array so it can be written to the CSV file
            String[] counts = new String[4];
            counts[0] = listOfPostalCodes.get(i);
            counts[1] = String.valueOf(amenityCount);
            counts[2] = String.valueOf(shopCount);
            counts[3] = String.valueOf(tourismCount);
            stuffForFile.add(counts);
        }
        
        try {
            // Write counts to CSV File
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