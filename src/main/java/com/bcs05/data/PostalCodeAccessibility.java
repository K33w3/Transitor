package com.bcs05.data;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostalCodeAccessibility {
    String postalCode;
    int amenityCount;
    int shopCount;
    int tourismCount;
    double latitude;
    double longitude;

    public int calculateAccessibilityScore() {
        return amenityCount + shopCount + tourismCount;
    }

    public static Map<String, Coordinates> readCoordinatesCSV(String fileName) {
        Map<String, Coordinates> coordinatesMap = new HashMap<>();
        String line;
        boolean firstLine = true;  // To skip the header line
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String postalCode = values[0].trim();
                    double latitude = Double.parseDouble(values[1].trim());
                    double longitude = Double.parseDouble(values[2].trim());
                    coordinatesMap.put(postalCode, new Coordinates(latitude, longitude));
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinatesMap;
    }

    public static List<PostalCodeAccessibility> readCSV(String fileName) {
        List<PostalCodeAccessibility> postalCodes = new ArrayList<>();
        String line;
        boolean firstLine = true;  // To skip the header line
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
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

    public static void writeCSV(String fileName, List<PostalCodeAccessibility> postalCodes, Map<String, Coordinates> coordinatesMap) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write the header
            bw.write("Lat,Lon,SEAI\n");
            for (PostalCodeAccessibility pca : postalCodes) {
                Coordinates coords = coordinatesMap.get(pca.postalCode);
                if (coords != null) {
                    pca.latitude = coords.latitude;
                    pca.longitude = coords.longitude;
                    bw.write(pca.latitude + "," + pca.longitude + "," + pca.calculateAccessibilityScore() + "\n");
                } else {
                    System.err.println("Coordinates not found for postal code: " + pca.postalCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeChangesCSV() {
        Map<String, Coordinates> coordinatesMap = readCoordinatesCSV("src/main/resources/MassZipLatLon.csv");
        List<PostalCodeAccessibility> postalCodes = readCSV("src/main/resources/countofammenities.csv");
        writeCSV("src/main/resources/postalAcc.csv", postalCodes, coordinatesMap);
    }

}

class Coordinates {
    double latitude;
    double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
