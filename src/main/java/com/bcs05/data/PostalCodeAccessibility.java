package com.bcs05.data;

import com.bcs05.util.Coordinates;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostalCodeAccessibility {
    private String postalCode;
    private int amenityCount;
    private int shopCount;
    private int tourismCount;
    private double latitude;
    private double longitude;

    /**
     * Calculates the accessibilty score based on the counts of amenities, shops and tourism spots
     * @return the accessibility score
     */
    public double calculateAccessibilityScore() {
        return 0.7* amenityCount + 0.2 * shopCount + 0.1*tourismCount;
    }

    /**
     * Reads coordinates from a CSV file and stores them in a map
     * @param fileName the name of the csv file containing the coordinates data
     * @return a map with postal codes as keys and Coordinates objects as values
     */
    public static Map<String, Coordinates> readCoordinatesCSV(String fileName) {
        Map<String, Coordinates> coordinatesMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean firstLine = true;  // To skip the header line
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String postalCode = values[0].trim();
                    String latitude = values[1].trim();
                    String longitude = values[2].trim();
                    // Store the coordinates in the map with postal code as the key
                    coordinatesMap.put(postalCode, new Coordinates(latitude, longitude, postalCode));
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinatesMap;
    }

    /**
     * Reads postal code data from a CSV file and stores them in a list
     * @param fileName the name of the CSV file containing the postal code data
     * @return a list of postalCodeAccessibility objects
     */
    public static List<PostalCodeAccessibility> readCSV(String fileName) {
        List<PostalCodeAccessibility> postalCodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean firstLine = true;  // To skip the header line
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 4) {  // Ensure there are enough columns
                    PostalCodeAccessibility pca = new PostalCodeAccessibility();
                    pca.postalCode = values[0].trim();
                    pca.amenityCount = Integer.parseInt(values[1].trim());
                    pca.shopCount = Integer.parseInt(values[2].trim());
                    pca.tourismCount = Integer.parseInt(values[3].trim());
                    postalCodes.add(pca);
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postalCodes;
    }

    /**
     * Writes the postal code accessibility data to a CSV file, including coordinates and accessibility scores
     * @param fileName the name of the CSV file to write to.
     * @param postalCodes a list of PostalCodeAccessibility objects containing the postal code data
     * @param coordinatesMap a map containing the coordinates for each postal code 
     */
    public static void writeCSV(String fileName, List<PostalCodeAccessibility> postalCodes, Map<String, Coordinates> coordinatesMap) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write the header
            bw.write("Lat,Lon,SEAI\n");
            for (PostalCodeAccessibility pca : postalCodes) {
                Coordinates coords = coordinatesMap.get(pca.postalCode);
                if (coords != null) {
                    pca.latitude = Double.parseDouble(coords.getLatitude());
                    pca.longitude = Double.parseDouble(coords.getLongitude());
                    bw.write(pca.latitude + "," + pca.longitude + "," + pca.calculateAccessibilityScore() + "\n");
                } else {
                    System.err.println("Coordinates not found for postal code: " + pca.postalCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the neccessary CSV file, processes the data, and writes the updated postal code accessebility data to a new CSV file
     */
    public void writeChangesCSV() {
        Map<String, Coordinates> coordinatesMap = readCoordinatesCSV("src/main/resources/MassZipLatLon.csv");
        List<PostalCodeAccessibility> postalCodes = readCSV("src/main/resources/countofammenities.csv");
        writeCSV("src/main/resources/postalAccUpdated.csv", postalCodes, coordinatesMap);
    } 
}
