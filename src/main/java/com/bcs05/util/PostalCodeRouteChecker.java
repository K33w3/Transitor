// package com.bcs05.util;

// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.PrintWriter;
// import java.util.ArrayList;
// import java.util.List;

// import com.bcs05.engine.GTFSEngineWithTransfers;

// public class PostalCodeRouteChecker {
//     private static GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();

//     private static List<String> readZipCodesFromCSV(String csvFile) {
//         List<String> zipCodes = new ArrayList<>();
//         try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
//             String line;
//             br.readLine();
//             while ((line = br.readLine()) != null) {
//                 String[] values = line.split(",");
//                 zipCodes.add(values[0]);
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//         return zipCodes;
//     }

//     private static boolean generateRoute(String from, String to) {
//         System.out.println("Generating route from " + from + " to " + to);
//         PathTransfer path = engine.findPathWithTransfers(from, to, 0.5);
//         if(path != null && !path.getCoordinates().isEmpty()){
//             return true;
//         }
//         return false;
//     }

//     public static void main(String[] args) {
//         String csvFile = "src/main/resources/MassZipLatLon.csv";
//         String outputCsvFile = "src/main/resources/validTransferRoutes.csv";
//         List<String> zipCodes = readZipCodesFromCSV(csvFile);

//         System.out.println("Total zip codes read: " + zipCodes.size());

//         try (PrintWriter pw = new PrintWriter(new FileWriter(outputCsvFile))) {
//             for (int i = 0; i < zipCodes.size(); i++) {
//                 for (int j = i + 1; j < zipCodes.size(); j++) {
//                     System.out.println("Checking route from " + zipCodes.get(i) + " to " + zipCodes.get(j));
//                     if (generateRoute(zipCodes.get(i+100), zipCodes.get(j))) {
//                         System.out.println("Valid route found: " + zipCodes.get(i) + "," + zipCodes.get(j));
//                         pw.println(zipCodes.get(i) + "," + zipCodes.get(j));
//                     } else {
//                         System.out.println("No valid route found: " + zipCodes.get(i) + " to " + zipCodes.get(j));
//                     }
//                 }
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
// }
package com.bcs05.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bcs05.engine.GTFSEngineWithTransfers;
import com.bcs05.util.PathTransfer;

public class PostalCodeRouteChecker {
    private static GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();

    private static List<String> readZipCodesFromCSV(String csvFile) {
        List<String> zipCodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                zipCodes.add(values[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipCodes;
    }

    private static List<String> getRandomSubset(List<String> zipCodes, double fraction) {
        Collections.shuffle(zipCodes);
        int subsetSize = (int) (zipCodes.size() * fraction);
        return zipCodes.subList(0, subsetSize);
    }

    private static boolean generateRoute(String from, String to) {
        PathTransfer path = engine.findPathWithTransfers(from, to, 0.5);
        return path != null && !path.getCoordinates().isEmpty();
    }

    public static void main(String[] args) {
        String csvFile = "src/main/resources/MassZipLatLon.csv";
        String outputCsvFile = "src/main/resources/resultsRouteCheckRandom.csv";
        List<String> zipCodes = readZipCodesFromCSV(csvFile);

        // Take a third of the postal codes
        List<String> subsetZipCodes = getRandomSubset(zipCodes, 1.0 / 3.0);
        System.out.println("Total subset zip codes: " + subsetZipCodes.size());

        Set<String> usedStartingPostalCodes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        Set<String> checkedRoutes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        Random rand = new Random();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputCsvFile, true))) { // Open file in append mode
            List<String> validRoutes = Collections.synchronizedList(new ArrayList<>());

            Runnable task = () -> {
                while (usedStartingPostalCodes.size() < subsetZipCodes.size()) {
                    String startingPostalCode;
                    do {
                        startingPostalCode = subsetZipCodes.get(rand.nextInt(subsetZipCodes.size()));
                    } while (usedStartingPostalCodes.contains(startingPostalCode));

                    usedStartingPostalCodes.add(startingPostalCode);

                    for (String toPostalCode : subsetZipCodes) {
                        if (!startingPostalCode.equals(toPostalCode)) {
                            String routeKey = startingPostalCode + "-" + toPostalCode;
                            if (!checkedRoutes.contains(routeKey)) {
                                if (generateRoute(startingPostalCode, toPostalCode)) {
                                    String validRoute = startingPostalCode + "," + toPostalCode;
                                    validRoutes.add(validRoute);
                                    synchronized (pw) {
                                        pw.println(validRoute);
                                        pw.flush(); // Ensure data is written to the file immediately
                                    }
                                }
                                checkedRoutes.add(routeKey);
                            }
                        }
                    }
                }
            };

            for (int i = 0; i < numThreads; i++) {
                executorService.submit(task);
            }

            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // Wait for all tasks to complete
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
