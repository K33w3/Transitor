package com.bcs05.engine;

import com.bcs05.util.Utils;
import com.graphhopper.ResponsePath;
import com.graphhopper.GraphHopper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.config.Profile;
import com.bcs05.util.Coordinates;
import com.bcs05.util.Transportation;
import java.math.BigDecimal;
import java.util.ArrayList;

public class RoutingEngine {

    private static final String OSM_FILE_PATH = "src/main/resources/Maastricht.osm.pbf";
    private GraphHopper hopper;
    private Transportation transportation;

    /**
     * Constructs a new RoutingEngine object with the specified transportation mode.
     *
     * @param transportation The mode of transportation for routing.
     */
    public RoutingEngine(Transportation transportation) {
        this.hopper = createGraphHopperInstance(OSM_FILE_PATH);
        this.transportation = transportation;
    }

    /**
     * Represents a routing engine that uses the GraphHopper library for route
     * calculation.
     *
     * @param ghLoc The location of the GraphHopper files.
     * @return The initialized GraphHopper instance.
     */
    private GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/routing-graph-cache");

        hopper.setProfiles(new Profile("foot").setVehicle("foot").setTurnCosts(false),
                new Profile("bike").setVehicle("bike").setTurnCosts(true));

        hopper.importOrLoad();

        return hopper;
    }

    /**
     * Calculates the best route between two coordinates using GraphHopper.
     *
     * @param from The starting coordinates.
     * @param to   The ending coordinates.
     * @return The best route as a ResponsePath object.
     * @throws RuntimeException if the routing request encounters errors.
     */
    protected ResponsePath routing(Coordinates from, Coordinates to) {

        // Create GraphHopper request
        GHRequest request = new GHRequest(Double.valueOf(from.getLatitude()),
                Double.valueOf(from.getLongitude()), Double.valueOf(to.getLatitude()),
                Double.valueOf(to.getLongitude())).setProfile(transportation.label);

        // Generate GraphHopper response
        GHResponse response = hopper.route(request);

        if (response.hasErrors()) {
            throw new RuntimeException(response.getErrors().toString());
        }

        return response.getBest();
    }

    /**
     * Calculates the estimated time for the best route between two coordinates.
     *
     * @param from The starting coordinates.
     * @param to   The ending coordinates.
     * @return The estimated time in minutes as a BigDecimal.
     */
    public BigDecimal getTime(Coordinates from, Coordinates to) {
        ResponsePath path = routing(from, to);
        return BigDecimal.valueOf(path.getTime() / 1000 / 60);
    }

    /**
     * Calculates the distance of the best route between two coordinates.
     *
     * @param from The starting coordinates.
     * @param to   The ending coordinates.
     * @return The distance in meters as a BigDecimal.
     */
    public BigDecimal getDistance(Coordinates from, Coordinates to) {
        ResponsePath path = routing(from, to);
        return BigDecimal.valueOf(path.getDistance());
    }

    /**
     * Retrieves the list of coordinates along the best route between two
     * coordinates.
     *
     * @param from The starting coordinates.
     * @param to   The ending coordinates.
     * @return The list of coordinates as an ArrayList.
     */
    public ArrayList<Coordinates> getPoints(Coordinates from, Coordinates to) {
        ResponsePath path = routing(from, to);
        ArrayList<Coordinates> points = Utils.pointListToArrayList(path.getPoints());

        return points;
    }
}
