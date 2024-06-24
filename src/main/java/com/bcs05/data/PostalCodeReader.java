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

    public PostalCodeReader() {
        records = new HashMap<>();
        populateRecords();
    }

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
                records.put(postalCode, coordinates);
            }
        } catch (IOException e) {
            System.out.println("Incorrect file path. The file was not found.");
        }
    }

    public Coordinates getCoordinates(String postalCode) {
        if (records.containsKey(postalCode)) {
            return records.get(postalCode);
        } else {
            return APIClient.callAPI(postalCode);
        }
    }

    public HashMap<String, Coordinates> getRecords() {
        return records;
    }
}
