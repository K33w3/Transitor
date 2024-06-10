package com.bcs05.visualization;

import com.bcs05.engine.DistanceCalculator;
import com.bcs05.engine.GTFSEngine;
import com.bcs05.engine.RoutingEngine;
import com.bcs05.engine.TimeCalculator;
import com.bcs05.util.CoordHandler;
import com.bcs05.util.Coordinates;
import com.bcs05.util.Path;
import com.bcs05.util.PathCoordinates;
import com.bcs05.util.PathStop;
import com.bcs05.util.RouteHandler;
import com.bcs05.util.Transportation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Represents the user interface for the route generator applications
 */
public class UI extends JFrame {

    private JPanel inputPanel;
    private JTextField startPostalCodeField;
    private JTextField endPostalCodeField;
    private String startPostalCode;
    private String endPostalCode;
    private String mode;
    private JButton generateButton;
    private JComboBox<String> modeSelection;
    private JLabel distanceLabel;
    private JLabel timeLabel;
    private JSlider radiusSlider;
    private JLabel radiusLabel;
    private JFXPanel jfxPanel;
    private WebEngine webEngine;

    public UI() {
        setTitle("Route Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        setIconImage(icon.getImage());

        initComponents();

        setSize(1920, 1080);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Add resize listener
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        Platform.runLater(() -> {
                            webEngine.reload(); // Reload the content to handle resizing
                            // Re-apply the updateMap function to ensure markers are added back
                        });
                    }
                });
    }

    private void initComponents() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize input panel
        inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding
        inputPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between components
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Route Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5); // Top padding of 20 pixels
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)), gbc);

        JLabel titleLabel2 = new JLabel("Group 5");
        titleLabel2.setFont(new Font("Arial", Font.BOLD, 18));
        inputPanel.add(titleLabel2, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Origin Postal Code:"), gbc);

        gbc.gridx = 1;
        startPostalCodeField = new JTextField(15);
        inputPanel.add(startPostalCodeField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Destination Postal Code:"), gbc);

        gbc.gridx = 1;
        endPostalCodeField = new JTextField(15);
        inputPanel.add(endPostalCodeField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Mode:"), gbc);

        gbc.gridx = 1;
        modeSelection = new JComboBox<>(new String[] { "Bike", "Foot", "Bus", "Aerial" });
        modeSelection.setPreferredSize(new Dimension(200, 25)); // Set preferred size
        inputPanel.add(modeSelection, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Radius (m):"), gbc);

        gbc.gridx = 1;
        radiusSlider = new JSlider(JSlider.HORIZONTAL, 100, 500, 300); // Min: 1km, Max: 50km, Initial: 5km
        radiusSlider.setEnabled(false); // Initially disabled
        inputPanel.add(radiusSlider, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        radiusLabel = new JLabel("Radius: 300m");
        radiusLabel.setEnabled(false); // Initially disabled
        inputPanel.add(radiusLabel, gbc);

        // Add a listener to update the radius label
        radiusSlider.addChangeListener(e -> radiusLabel.setText("Radius: " + radiusSlider.getValue() + " m"));

        modeSelection.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (modeSelection.getSelectedItem().equals("Bus")) {
                    radiusSlider.setEnabled(true);
                    radiusLabel.setEnabled(true);
                } else {
                    radiusSlider.setEnabled(false);
                    radiusLabel.setEnabled(false);
                }
            }
        });

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        distanceLabel = new JLabel("Distance: N/A");
        inputPanel.add(distanceLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        timeLabel = new JLabel("Time: N/A");
        inputPanel.add(timeLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        generateButton = new JButton("Generate");
        generateButton.setBackground(new Color(76, 175, 80)); // Green color
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(e -> {
            startPostalCode = startPostalCodeField.getText();
            endPostalCode = endPostalCodeField.getText();
            mode = (String) modeSelection.getSelectedItem();

            if (!isModeValid(mode) || !isStartEndValid(startPostalCode, endPostalCode)) {
                showError("Please select a mode and enter valid postal codes.");
            } else {
                clearOldRoute();
                chooseRoute(mode);
            }
        });
        inputPanel.add(generateButton, gbc);

        add(inputPanel, BorderLayout.WEST);

        // Initialize the JavaFX panel
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        // Create the JavaFX scene
        Platform.runLater(this::createJavaFXScene);
    }

    private void clearOldRoute() {
        Platform.runLater(() -> {
            webEngine.executeScript("updateMap([]); drawRoute([]);");
            webEngine.executeScript("clearRouteDescription();");

        });
    }

    private void clearDescription() {
        Platform.runLater(() -> {
            webEngine.executeScript("clearRouteDescription();");
        });
    }

    private void chooseRoute(String mode) {
        switch (mode) {
            case "Bus":
                generateRouteGtfs();
                break;
            case "Bike":
                generateRouteGraphhopper();
                break;
            case "Foot":
                generateRouteGraphhopper();
                break;
            case "Aerial":
                generateAerialDistance();
                break;
            default:
                showError(mode + " is not a valid mode.");
        }
    }

    private boolean isStartEndValid(String startPostalCode, String endPostalCode) {
        Coordinates origin = CoordHandler.getCoordinates(startPostalCode);
        Coordinates destination = CoordHandler.getCoordinates(endPostalCode);
        if (origin == null || destination == null) {
            showError("Invalid postal code(s).");
            return false;
        }
        return true;
    }

    private boolean isModeValid(String mode) {
        Transportation mode1 = getTransportationMode(mode);
        if (mode1 == null) {
            showError("Invalid mode.");
            return false;
        }
        return true;
    }

    private void createJavaFXScene() {
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
        clearDescription();
    }

    private void generateAerialDistance() {
        clearDescription();
        Coordinates origin = CoordHandler.getCoordinates(startPostalCode);
        Coordinates destination = CoordHandler.getCoordinates(endPostalCode);

        if (origin == null || destination == null) {
            showError("Invalid postal code(s).");
            return;
        }

        List<Coordinates> route = new ArrayList<>();
        route.add(origin);
        route.add(destination);

        String jsCommand = buildJsCommandList(route);
        Platform.runLater(() -> webEngine.executeScript(jsCommand));
        updateUIComponentsAerialDistance(origin, destination);
    }

    private void generateRouteGraphhopper() {
        clearDescription();
        Coordinates origin = CoordHandler.getCoordinates(startPostalCode);
        Coordinates destination = CoordHandler.getCoordinates(endPostalCode);

        RoutingEngine routeEngine = new RoutingEngine(Transportation.valueOf(mode.toUpperCase()));

        List<Coordinates> route = routeEngine.getPoints(origin, destination);

        if (route == null || route.isEmpty()) {
            showError("Could not generate route.");
            return;
        }

        String jsCommand = buildJsCommandList(route);
        Platform.runLater(() -> webEngine.executeScript(jsCommand));

        updateUIComponentsGraph(mode, origin, destination);
    }

    private void generateRouteGtfs() {
        clearOldRoute();
        // clearDescription();
        int radius = radiusSlider.getValue(); // Get the radius in m

        GTFSEngine rouGtfsEngine = new GTFSEngine();
        Path route = rouGtfsEngine.findShortestDirectPath(startPostalCode, endPostalCode, radius / 1000.0); // Convert
                                                                                                            // to
                                                                                                            // appropriate
                                                                                                            // scale

        if (route == null || route.getCoordinates().isEmpty()) {
            showError("Could not generate route.");
            return;
        }

        String jsCommand = buildJsCommand(route.getCoordinates());
        Platform.runLater(() -> webEngine.executeScript(jsCommand));

        updateUIComponentsGTFS(route);
        updateRouteDescription(route);

        showRouteDescription();
    }

    private void showRouteDescription() {
        Platform.runLater(() -> {
            webEngine.executeScript("showRouteDescription();");
        });
    }

    private void updateRouteDescription(Path route) {
        StringBuilder description = new StringBuilder();
        description.append("<strong>Route Description:</strong><br/><br/>");

        for (PathStop coord : route.getStops()) {
            description.append("Stop: ").append(coord.getName()).append("<br/>");
            description.append("Time: ").append(coord.getDepartureTime()).append("<br/>");
        }

        String finalDescription = description.toString();

        Platform.runLater(() -> {
            webEngine.executeScript("updateRouteDescription('" + finalDescription.replace("'", "\\'") + "');");
        });
    }

    private Transportation getTransportationMode(String mode) {
        try {
            return Transportation.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String buildJsCommand(ArrayList<PathCoordinates> route) {
        StringBuilder routeArray = new StringBuilder("[");
        for (Coordinates coord : route) {
            routeArray.append("[").append(coord.getLatitude()).append(", ").append(coord.getLongitude()).append("],");
        }
        routeArray.setLength(routeArray.length() - 1); // Remove the trailing comma
        routeArray.append("]");
        return "drawRoute(" + routeArray.toString() + ");";
    }

    private String buildJsCommandList(List<Coordinates> route) {
        StringBuilder routeArray = new StringBuilder("[");
        for (Coordinates coord : route) {
            routeArray.append("[").append(coord.getLatitude()).append(", ").append(coord.getLongitude()).append("],");
        }
        routeArray.setLength(routeArray.length() - 1); // Remove the trailing comma
        routeArray.append("]");
        return "drawRoute(" + routeArray.toString() + ");";
    }

    private void updateUIComponentsGraph(String mode, Coordinates start, Coordinates end) {
        double distance = RouteHandler.getDistanceRoute(mode, start, end);
        double time = calculateTime(mode, start, end);
        time = Math.round(time * 100.0) / 100.0;
        distance = Math.round(distance * 100.0) / 100.0;

        distanceLabel.setText("Distance: " + distance + " m");
        timeLabel.setText("Time: " + time + " minutes");
    }

    private void updateUIComponentsGTFS(Path route) {
        distanceLabel.setText("Distance: " + route.getDistance() + " meters");
        timeLabel.setText("Time: " + route.getTime().toMinutes() + " minutes");
    }

    private void updateUIComponentsAerialDistance(Coordinates origin, Coordinates destination) {
        distanceLabel.setText("Distance: " + (int) (calculateAerialDistance(origin, destination) * 1000) + " m");
        timeLabel.setText("Time: " + (int) (((calculateAerialDistance(origin, destination) * 1000) / 20)) + " seconds"); // You're
                                                                                                                         // flying
                                                                                                                         // at
                                                                                                                         // 20
                                                                                                                         // m/s
    }

    private double calculateAerialDistance(Coordinates origin, Coordinates destination) {
        return DistanceCalculator.calculateAerialDistance(
                new BigDecimal(origin.getLatitude()),
                new BigDecimal(origin.getLongitude()),
                new BigDecimal(destination.getLatitude()),
                new BigDecimal(destination.getLongitude())).doubleValue();
    }

    private double calculateTime(String mode, Coordinates origin, Coordinates destination) {
        if (mode.equalsIgnoreCase("Bike")) {
            return TimeCalculator
                    .cycleTime(
                            new BigDecimal(origin.getLatitude()),
                            new BigDecimal(origin.getLongitude()),
                            new BigDecimal(destination.getLatitude()),
                            new BigDecimal(destination.getLongitude()))
                    .doubleValue();
        } else {
            return TimeCalculator
                    .walkTime(
                            new BigDecimal(origin.getLatitude()),
                            new BigDecimal(origin.getLongitude()),
                            new BigDecimal(destination.getLatitude()),
                            new BigDecimal(destination.getLongitude()))
                    .doubleValue();
        }
    }
}
