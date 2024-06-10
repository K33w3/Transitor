package com.bcs05.util;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import java.util.ArrayList;

/**
 * The Utils class provides utility methods for various operations.
 */
public class Utils {

    /**
     * Converts a PointList to an ArrayList of Coordinates.
     *
     * @param pl the PointList to be converted
     * @return an ArrayList of Coordinates
     */
    public static ArrayList<Coordinates> pointListToArrayList(PointList pl) {
        ArrayList<Coordinates> points = new ArrayList<Coordinates>();

        for (GHPoint ghp : pl) {
            Coordinates point = new Coordinates(String.valueOf(ghp.getLat()), String.valueOf(ghp.getLon()));
            points.add(point);
        }

        return points;
    }

}
