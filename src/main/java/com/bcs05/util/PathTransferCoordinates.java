package com.bcs05.util;

public class PathTransferCoordinates extends PathCoordinates {

    private int colorId;

    public PathTransferCoordinates(String latitude, String longitude, int shapeDistTraveled, int type, int colorId) {
        super(latitude, longitude, shapeDistTraveled, type);
        this.colorId = colorId;
    }

}
