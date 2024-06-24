package com.bcs05.util;

import java.util.ArrayList;

public class PathTransfer extends Path {

    private ArrayList<Route> routes;

    public PathTransfer() {
        super();
        this.routes = new ArrayList<Route>();
    }

    public PathTransfer(ArrayList<PathCoordinates> path, ArrayList<PathStop> stops, ArrayList<Route> routes) {
        super(path, stops);
        this.routes = routes;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

}
