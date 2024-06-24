package com.bcs05.util;

import java.time.LocalTime;
import java.util.ArrayList;

public class BusTransferResult {
    private ArrayList<PathTransferStop> stops;
    private LocalTime arrivalTime;

    public BusTransferResult(ArrayList<PathTransferStop> stops, LocalTime arrivalTime) {
        this.stops = stops;
        this.arrivalTime = arrivalTime;
    }

    public ArrayList<PathTransferStop> getStops() {
        return stops;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
}
