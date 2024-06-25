package com.bcs05.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bcs05.engine.GTFSEngineWithTransfers;

/**
 * Analysis the time performance of route generation using GTFSEngineWithTransfers
 */
public class TimePerformanceAnalysis {

    /**
     * Reads postal code pairs from a CSV file
     * 
     * @param csvFile The path to the CSV file containing postal code pairs
     * @return A list of postal code pairs read from the CSV file
     */
    private static List<String[]> readZipCodesFromCSV(String csvFile) {
        List<String[]> zipCodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                zipCodes.add(new String[] { values[0], values[1] });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipCodes;
    }

    /**
     * Writes timing results to a CSV file
     * 
     * @param csvFile The path to the CSV file to write results
     * @param results The list of timing results to write to the file
     */
    private static void writeResultsToCSV(String csvFile, List<String[]> results) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            bw.write("postal_Code_start,postal_code_end,time\n"); // Writing header
            for (String[] result : results) {
                bw.write(String.join(",", result) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Measures the time taken to generate a route between two postal codes using GTFSEngineWithTransfers
     * 
     * @param from The starting postal code
     * @param to The destination postal code
     * @return The time taken in milliseconds to generate the route
     */
    private long timeTakenGenerateRoute(String from, String to) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        long startTime = System.currentTimeMillis();
        engine.findPathWithTransfers(from, to, 0.5);
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    public static void main(String[] args) {
        TimePerformanceAnalysis analysis = new TimePerformanceAnalysis();
        
        String inputCsvFile = "src/main/resources/selected_random_pairs.csv"; // Input CSV file with postal code pairs
        String outputCsvFile = "src/main/resources/timing_results.csv"; // Output CSV file to write timing results
    
        List<String[]> zipCodes = readZipCodesFromCSV(inputCsvFile); // Read postal code pairs from CSV file
        List<String[]> results = new ArrayList<>();

        // Measure Time taken for each given postal code pair and store results
        for (String[] pair : zipCodes) {
            String from = pair[0];
            String to = pair[1];
            long time = analysis.timeTakenGenerateRoute(from, to); // Measure time to generate route
            results.add(new String[] { from, to, Long.toString(time) }); // store results
        }

        writeResultsToCSV(outputCsvFile, results); // Write timing results to CSV file
    }
}
