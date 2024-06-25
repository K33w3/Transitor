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
 * Used to analyze memory usage and execution time for generating routes
 * between postal code pairs using GTFSEngineWithTransfers
 */
public class MemoryUsageAnalysis {

    /**
     * Reads postal code pairs from a CSV file
     * 
     * @param csvFile The path to the CSV file containing postal code pairs
     * @return A list of String arrays where each array contains two postal codes
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
     * Writes results to a CSV file
     * 
     * @param csvFile The path to the CSV file to write results to
     * @param result The results to write to the CSV file as a String array
     */
    private static void writeResultsToCSV(String csvFile, String[] result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true))) {
            bw.write(String.join(",", result) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a header to a CSV file
     * 
     * @param csvFile The path to the CSV file to write header to
     */
    private static void writeHeaderToCSV(String csvFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            bw.write("postal_Code_start,postal_code_end,time,memory_used(MB)\n"); // Writing header
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs the route generation and measures the time and memory used
     * 
     * @param from the starting postal code 
     * @param to The destination postal code
     * @return an Array containing the time taken in milliseconds and memory used in MB
     */
    private long[] timeAndMemoryTakenGenerateRoute(String from, String to) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        Runtime runtime = Runtime.getRuntime();
        
        // Garbage collection to get a more accurate memory usage
        runtime.gc();
        
        long startTime = System.currentTimeMillis();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        
        engine.findPathWithTransfers(from, to, 0.5);
        
        long endTime = System.currentTimeMillis();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long timeTaken = endTime - startTime;
        long memoryUsedBytes = endMemory - startMemory;
        
        long memoryUsedMB = memoryUsedBytes / (1024 * 1024);
        
        return new long[] { timeTaken, memoryUsedMB };
    }

    public static void main(String[] args) {

        MemoryUsageAnalysis analysis = new MemoryUsageAnalysis();
        String inputCsvFile = "src/main/resources/selected_random_pairs.csv";
        String outputCsvFile = "src/main/resources/memory_results.csv";
    
        List<String[]> zipCodes = readZipCodesFromCSV(inputCsvFile);

        // Write header to CSV file
        writeHeaderToCSV(outputCsvFile);

        for (String[] pair : zipCodes) {
            String from = pair[0];
            String to = pair[1];
            long[] timeAndMemory = analysis.timeAndMemoryTakenGenerateRoute(from, to);
            String[] result = new String[] { from, to, Long.toString(timeAndMemory[0]), Long.toString(timeAndMemory[1]) };
            writeResultsToCSV(outputCsvFile, result);
        }
    }
}
