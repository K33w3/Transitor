package com.bcs05;

import javax.swing.JFrame;

import com.bcs05.visualization.UI;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        UI ui = new UI();
        ui.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setVisible(true);
    }
}
