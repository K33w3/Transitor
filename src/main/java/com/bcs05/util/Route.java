package com.bcs05.util;

public class Route {

    private String routeId;
    private String routeShortName; // bus number
    private String routeLongName;

    public Route(String routeId, String routeShortName, String routeLongName) {
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    @Override
    public String toString() {
        return routeId;
    }

}
