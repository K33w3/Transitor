package com.bcs05;

import javax.swing.JFrame;

import com.bcs05.visualization.UI;
import com.bcs05.util.JSONAccessabilityScores;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        JSONAccessabilityScores.writeToCSVFile();
        UI ui = new UI();
        ui.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setVisible(true);
    }
}
