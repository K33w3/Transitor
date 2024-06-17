package com.bcs05;

import javax.swing.JFrame;
import java.util.ArrayList;
import com.bcs05.util.jsonCoordinateArray;
import com.bcs05.visualization.UI;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<String>> list = jsonCoordinateArray.createCoordinateArray();
        System.out.println(list);
        UI ui = new UI();
        ui.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setVisible(true);
    }
}
