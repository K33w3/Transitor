package com.bcs05.visualization;

import com.bcs05.engine.DistanceCalculator;
import com.bcs05.engine.GTFSEngine;
import com.bcs05.engine.GTFSEngineWithTransfers;
import com.bcs05.engine.RoutingEngine;
import com.bcs05.engine.TimeCalculator;
import com.bcs05.util.CoordHandler;
import com.bcs05.util.Coordinates;
import com.bcs05.util.Path;
import com.bcs05.util.PathCoordinates;
import com.bcs05.util.PathStop;
import com.bcs05.util.PathTransfer;
import com.bcs05.util.RouteHandler;
import com.bcs05.util.Transportation;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import netscape.javascript.JSObject;

public class UI extends JFrame {

    private JFXPanel jfxPanel;
    private WebEngine webEngine;
    private double distance;
    private double time;
    private Path routeBus;

    public UI() {
        setTitle("Route Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));
        setIconImage(icon.getImage());

        initComponents();

        setSize(1920, 1080);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        Platform.runLater(this::createJavaFXScene);
    }

    private void initComponents() {
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
    }

    private void createJavaFXScene() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        webEngine
            .getLoadWorker()
            .stateProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaUI", this);
                }
            });

        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
    }

    public void createJsonJavascript(String fromPostal, String toPostal, String mode, int range) {
        if (isStartEndValid(fromPostal, toPostal)) {
            List<Coordinates> coordinates = chooseRoute(fromPostal, toPostal, mode, range);
    
            Map<String, Object> routeDetails = new HashMap<>();
            routeDetails.put("fromPostal", fromPostal);
            routeDetails.put("toPostal", toPostal);
            routeDetails.put("mode", mode);
            routeDetails.put("time", whichTime(mode, fromPostal, toPostal));
            routeDetails.put("distance", (Math.round(distance * 100.0) / 100.0));
            routeDetails.put("range", range);
            routeDetails.put("details", "Route from " + fromPostal + " to " + toPostal + " by " + mode);
    
            if (mode.equals("bus") || mode.equals("transit")) {
                routeDetails.put("stops", convertStopsToMapList(routeBus.getStops()));
                routeDetails.put("coordinates", convertCoordinatesToJsArrayBus(routeBus));
                System.out.println("Transit route chosen: " + routeBus.getStops().size() + " stops");
            } else {
                String coordinatesJsArray = convertCoordinatesToJsArray(coordinates);
                routeDetails.put("coordinates", coordinatesJsArray);
            }
    
            String routeDetailsJson = new Gson().toJson(routeDetails);
    
            String escapedJson = routeDetailsJson.replace("\\", "\\\\").replace("\"", "\\\"");
            String jsCode = "receiveRouteDetails(\"" + escapedJson + "\");";
    
            Platform.runLater(() -> {
                webEngine.executeScript(jsCode);
            });
        }
    }
    

    private boolean isStartEndValid(String startPostalCode, String endPostalCode) {
        try {
            Coordinates origin = CoordHandler.getCoordinates(startPostalCode);
            Coordinates destination = CoordHandler.getCoordinates(endPostalCode);

            if (origin == null || destination == null) {
                Platform.runLater(() -> {
                    webEngine.executeScript("displayError(\"Invalid postal codes. Please try again.\");");
                });
                return false;
            }
            return true;
        } catch (Exception e) {
            Platform.runLater(() -> {
                webEngine.executeScript("displayError(\"Invalid postal codes. Please try again.\");");
            });
            return false;
        }
    }

    private double whichTime(String mode, String fromPostal, String toPostal) {
        switch (mode) {
            case "bus":
                return time;
            case "bike":
                return (
                    Math.round(
                        calculateTime(
                            mode,
                            CoordHandler.getCoordinates(fromPostal),
                            CoordHandler.getCoordinates(toPostal)
                        ) *
                        100.0
                    ) /
                    100.0
                );
            case "foot":
                return (
                    Math.round(
                        calculateTime(
                            mode,
                            CoordHandler.getCoordinates(fromPostal),
                            CoordHandler.getCoordinates(toPostal)
                        ) *
                        100.0
                    ) /
                    100.0
                );
            case "aerial":
                return (
                    (
                        Math.round(
                            calculateAerialDistance(
                                CoordHandler.getCoordinates(fromPostal),
                                CoordHandler.getCoordinates(toPostal)
                            ) *
                            1000
                        ) /
                        20
                    ) /
                    60
                );
            default:
                return 0;
        }
    }

    private List<Map<String, Object>> convertStopsToMapList(List<PathStop> stops) {
        List<Map<String, Object>> stopList = new ArrayList<>();
        if (stops != null && !stops.isEmpty()) {
            for (PathStop stop : stops) {
                Map<String, Object> stopMap = new HashMap<>();
                stopMap.put("name", stop.getName());
                stopMap.put("time", stop.getDepartureTime().toString());
                stopList.add(stopMap);
            }
        }
        return stopList;
    }

    private String convertCoordinatesToJsArrayBus(Path route) {
        if (route.getCoordinates() == null || route == null) {
            return "[]";
        }
    
        StringBuilder sb = new StringBuilder("[");
        try {
            for (int i = 0; i < route.getCoordinates().size(); i++) {
                PathCoordinates coord = route.getCoordinates().get(i);
                sb.append("[")
                  .append(coord.getLatitude()).append(", ")
                  .append(coord.getLongitude()).append(", ")
                  .append(coord.getType()).append("]");
                if (i < route.getCoordinates().size() - 1) {
                    sb.append(", ");
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                webEngine.executeScript("displayError(\"Error converting bus coordinates to JS array\");");
            });
        }
        sb.append("]");
        return sb.toString();
    }
    



    private String convertCoordinatesToJsArray(List<Coordinates> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        try {
            for (int i = 0; i < coordinates.size(); i++) {
                Coordinates coord = coordinates.get(i);
                sb.append("[").append(coord.getLatitude()).append(", ").append(coord.getLongitude()).append("]");
                if (i < coordinates.size() - 1) {
                    sb.append(", ");
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                webEngine.executeScript("displayError(\"Converting Coordinates to Js Array\");");
            });
        }
        sb.append("]");
        return sb.toString();
    }

    private List<Coordinates> chooseRoute(String fromPostal, String toPostal, String mode, int range) {

        switch (mode) {
            case "bus":
                return generateRouteGtfs(fromPostal, toPostal, range);
            case "bike":
                return generateRouteGraphhopper(fromPostal, toPostal, mode);
            case "foot":
                return generateRouteGraphhopper(fromPostal, toPostal, mode);
            case "aerial":
                return generateAerialDistance(fromPostal, toPostal);
            case "transit":
                return generateRouteTransfers(fromPostal, toPostal, range);
            default:
                return null;
        }
    }

    private List<Coordinates> generateAerialDistance(String fromPostal, String toPostal) {
        Coordinates origin = CoordHandler.getCoordinates(fromPostal);
        Coordinates destination = CoordHandler.getCoordinates(toPostal);

        List<Coordinates> route = new ArrayList<>();
        route.add(origin);
        route.add(destination);
        distance = calculateAerialDistance(origin, destination);
        return route;
    }

    private List<Coordinates> generateRouteGraphhopper(String fromPostal, String toPostal, String mode) {
        Coordinates origin = CoordHandler.getCoordinates(fromPostal);
        Coordinates destination = CoordHandler.getCoordinates(toPostal);

        RoutingEngine routeEngine = new RoutingEngine(getTransportationMode(mode));
        distance = RouteHandler.getDistanceRoute(mode, origin, destination);
        return routeEngine.getPoints(origin, destination);
    }
     

    private List<Coordinates> generateRouteTransfers(String fromPostal, String toPostal, int range) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        PathTransfer route = engine.findPathWithTransfers(fromPostal, toPostal, (double) range / 100.0);
        routeBus = route;
        distance = route.getDistance();
        time = route.getTime().toMinutes();
        List<Coordinates> routeCoords = new ArrayList<>();
    
        for (PathCoordinates pathCoord : route.getCoordinates()) {
            routeCoords.add(new Coordinates(pathCoord.getLatitude(), pathCoord.getLongitude()));
        }

        return routeCoords;
    }

    private List<Coordinates> generateRouteGtfs(String fromPostal, String toPostal, int range) {
        
        Path route = new GTFSEngine().findShortestDirectPath(fromPostal, toPostal, range / 100.0);
        distance = route.getDistance();
        time = route.getTime().toMinutes();
        routeBus = route;
        List<Coordinates> routeCoords = new ArrayList<>();
        for (PathCoordinates pathCoord : route.getCoordinates()) {
            routeCoords.add(new Coordinates(pathCoord.getLatitude(), pathCoord.getLongitude()));
        }

        return routeCoords;
}

    private Transportation getTransportationMode(String mode) {
        try {
            return Transportation.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private double calculateAerialDistance(Coordinates origin, Coordinates destination) {
        return DistanceCalculator
            .calculateAerialDistance(
                new BigDecimal(origin.getLatitude()),
                new BigDecimal(origin.getLongitude()),
                new BigDecimal(destination.getLatitude()),
                new BigDecimal(destination.getLongitude())
            )
            .doubleValue();
    }

    private double calculateTime(String mode, Coordinates origin, Coordinates destination) {
        if (mode.equalsIgnoreCase("Bike")) {
            return TimeCalculator
                .cycleTime(
                    new BigDecimal(origin.getLatitude()),
                    new BigDecimal(origin.getLongitude()),
                    new BigDecimal(destination.getLatitude()),
                    new BigDecimal(destination.getLongitude())
                )
                .doubleValue();
        } else {
            return TimeCalculator
                .walkTime(
                    new BigDecimal(origin.getLatitude()),
                    new BigDecimal(origin.getLongitude()),
                    new BigDecimal(destination.getLatitude()),
                    new BigDecimal(destination.getLongitude())
                )
                .doubleValue();
        }
    }
}
