package com.bcs05;

import java.io.IOException;

import javax.swing.JFrame;

import com.bcs05.data.GeoJSONReader;
import com.bcs05.visualization.UI;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            String geoJsonContent = GeoJSONReader.readFile("src/main/resources/amenity.geojson");
            System.out.println(geoJsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        UI ui = new UI();
        ui.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setVisible(true);
    }
}
