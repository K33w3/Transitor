package com.bcs05.data;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class GeoJSONReader {

    /**
     * Reads the GeoJSON File and returns the string value of the contents of the file given in the parameter
     * @param FILE_PATH The File path of the GeoJSON File
     * @return The String value of the GeoJSON contents
     * @throws IOException If an error occurs while reading the file
     */
    public static String readFile(String FILE_PATH) throws IOException {
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