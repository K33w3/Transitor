package com.bcs05.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class PostalCodeAccessibility {
    private String postalCode;
    private int amenityCount;
    private int shopCount;
    private int tourismCount;

    public PostalCodeAccessibility(String postalCode, int amenityCount, int shopCount, int tourismCount) {
        this.postalCode = postalCode;
        this.amenityCount = amenityCount;
        this.shopCount = shopCount;
        this.tourismCount = tourismCount;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public int getAmenityCount() {
        return amenityCount;
    }

    public int getShopCount() {
        return shopCount;
    }

    public int getTourismCount() {
        return tourismCount;
    }

    public int calculateAccessibilityScore() {
        return amenityCount + shopCount + tourismCount;
    }

    @Override
    public String toString() {
        return "PostalCodeAccessibility{" +
                "postalCode='" + postalCode + '\'' +
                ", amenityCount=" + amenityCount +
                ", shopCount=" + shopCount +
                ", tourismCount=" + tourismCount +
                ", accessibilityScore=" + calculateAccessibilityScore() +
                '}';
    }

    public static List<PostalCodeAccessibility> readCSV(String fileName) {
        List<PostalCodeAccessibility> postalCodes = new ArrayList<>();
        String line;
        boolean firstLine = true;  // To skip the header line
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header line
                    continue;
                }
                String[] values = line.split(",");
                String postalCode = values[0].trim();
                int amenityCount = Integer.parseInt(values[1].trim());
                int shopCount = Integer.parseInt(values[2].trim());
                int tourismCount = Integer.parseInt(values[3].trim());
                PostalCodeAccessibility pca = new PostalCodeAccessibility(postalCode, amenityCount, shopCount, tourismCount);
                postalCodes.add(pca);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postalCodes;
    }

    public static void writeCSV(String fileName, List<PostalCodeAccessibility> postalCodes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write the header
            bw.write("Postal Code,Amenity Count,Shop Count, Tourism Count, Accessibility Score\n");
            for (PostalCodeAccessibility pca : postalCodes) {
                bw.write(pca.getPostalCode() + "," +
                        pca.getAmenityCount() + "," +
                        pca.getShopCount() + "," +
                        pca.getTourismCount() + "," +
                        pca.calculateAccessibilityScore() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeChangesCSV() {
        List<PostalCodeAccessibility> postalCodes = readCSV("src/main/resources/countofammenities.csv");
        writeCSV("src/main/resources/postalAcc.csv", postalCodes);
    }

 
}