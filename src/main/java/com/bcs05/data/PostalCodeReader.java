package com.bcs05.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.bcs05.api.APIClient;
import com.bcs05.util.Coordinates;

public class PostalCodeReader {

    private static final String FILE_PATH = "src/main/resources/MassZipLatLon.csv";
    private HashMap<String, Coordinates> records;

    /**
     * Initializes the records map and populates it with data from the CSV file
     */
    public PostalCodeReader() {
        records = new HashMap<>();
        populateRecords();
    }

    /**
     * Reads the CSV file and populates the records map with postal codes and their coordinates
     * This method is called by the constructor to initialize the records map
     */
    private void populateRecords() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String row;
            br.readLine(); // skips the first row 'Zip,Lat,Lon' that contains the column names
            while ((row = br.readLine()) != null) {
                String[] values = row.split(",");
                String postalCode = values[0].trim();
                String latitude = values[1].trim();
                String longitude = values[2].trim();
                Coordinates coordinates = new Coordinates(latitude, longitude, postalCode);
                records.put(postalCode, coordinates); // Store the coordinates in the map
            }
        } catch (IOException e) {
            System.out.println("Incorrect file path. The file was not found.");
        }
    }

    /**
     * Retrieves the coordinates for a given postal code. If the postal code is not found in the records, it calls the API to fetch the coordinates
     * @param postalCode the postal code for which to retrieve the coordinates
     * @return the Coordinates object containing latitude and longitude 
     */
    public Coordinates getCoordinates(String postalCode) {
        if (records.containsKey(postalCode)) {
            return records.get(postalCode); // Returns coordinates if postal code is found in the records 
        } else {
            return APIClient.callAPI(postalCode); // Call API to fetch coordinates if postal code is not found
        }
    }

    /**
     * Returns the map of postal codes and their corresponding coordinates
     * @return the Hashmap containing the records 
     */
    public HashMap<String, Coordinates> getRecords() {
        return records;
    }
}
