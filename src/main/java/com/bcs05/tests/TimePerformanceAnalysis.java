package com.bcs05.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bcs05.engine.GTFSEngineWithTransfers;

public class TimePerformanceAnalysis {

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

    private long timeTakenGenerateRoute(String from, String to) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        long startTime = System.currentTimeMillis();
        engine.findPathWithTransfers(from, to, 0.5);
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    public static void main(String[] args) {
        TimePerformanceAnalysis analysis = new TimePerformanceAnalysis();
        
        String inputCsvFile = "src/main/resources/selected_random_pairs.csv";
        String outputCsvFile = "src/main/resources/timing_results.csv";
    
        List<String[]> zipCodes = readZipCodesFromCSV(inputCsvFile);
        List<String[]> results = new ArrayList<>();

        for (String[] pair : zipCodes) {
            String from = pair[0];
            String to = pair[1];
            long time = analysis.timeTakenGenerateRoute(from, to);
            results.add(new String[] { from, to, Long.toString(time) });
        }

        writeResultsToCSV(outputCsvFile, results);
    }
}
