package com.bcs05.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bcs05.engine.GTFSEngineWithTransfers;

public class MemoryUsageAnalysis {

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

    private static void writeResultsToCSV(String csvFile, String[] result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true))) {
            bw.write(String.join(",", result) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeaderToCSV(String csvFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            bw.write("postal_Code_start,postal_code_end,time,memory_used(MB)\n"); // Writing header
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
