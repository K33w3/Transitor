package com.bcs05.data;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class GeoJSONReader {
    private static final String FILE_PATH = "src/main/resources/amenity.geojson";

    public static String readFile() throws IOException {
        StringBuilder geoJSONContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                geoJSONContents.append(currentLine);
                geoJSONContents.append(System.lineSeparator());
            }
             
        }
        return geoJSONContents.toString();
    }
}