package com.bcs05.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.io.IOException;

import com.bcs05.api.APIClient;
import com.bcs05.util.Coordinates;

public class PostalCodeReader {

    private static final String FILE_PATH = "src/main/resources/MassZipLatLon.csv";

    public PostalCodeReader() {
        records = new HashMap<String, Coordinates>();
        populateRecords();
    }

    /**
     * This is the main method which makes use of addNum method.
     * 
     * @return Nothing.
     * @exception IOException On incorrect file path.
     */
    private void populateRecords() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String row;
            br.readLine(); // skips the first row 'Zip,Lat,Lon' that contains the column names
            while ((row = br.readLine()) != null) {
                String[] values = row.split(",");
                String postalCode = values[0];
                String latitude = values[1];
                String longitude = values[2];
                Coordinates coordinates = new Coordinates(latitude, longitude, postalCode);
                records.put(postalCode, coordinates);
            }
        } catch (IOException e) {
            System.out.println("Incorrect file path. The file was not found.");
        }
    }

    /**
     * This method returns the coordinates of the given postal code.
     * If the postal code is not found in the records, it calls the API to get the
     * coordinates.
     * 
     * @param postalCode The postal code in as a String data type.
     * @return The coordinates as Coordinates data type.
     */
    public Coordinates getCoordinates(String postalCode) {
        if (records.containsKey(postalCode)) {
            return records.get(postalCode);
        } else {
            return APIClient.callAPI(postalCode);
        }
    }

    /**
     * This method returns the records. Useful for displaying the records in the UI.
     * 
     * @return The records as a HashMap data type.
     */
    public HashMap<String, Coordinates> getRecords() {
        return records;
    }

    private HashMap<String, Coordinates> records;
}
