package com.bcs05.data;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class GeoJSONReader {
    private static final String FILE_PATH = "src/main/resources/amenity.geojson";

    public static void main(String[] args) {
        try {
            String geoJsonContent = readFile(FILE_PATH);
            System.out.println(geoJsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath) throws IOException {
        StringBuilder geoJSONContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                geoJSONContents.append(currentLine);
                geoJSONContents.append(System.lineSeparator());
            }
            
            
        }
        return geoJSONContents.toString();
    }
}