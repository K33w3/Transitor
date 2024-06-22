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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private static boolean generateRoute(String from, String to) {
        System.out.println("Generating route from " + from + " to " + to);
        PathTransfer path = engine.findPathWithTransfers(from, to, 0.5);
        if (path != null && !path.getCoordinates().isEmpty()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String csvFile = "src/main/resources/MassZipLatLon.csv";
        String outputCsvFile = "src/main/resources/resultsRouteCheckRandom.csv";
        List<String> zipCodes = readZipCodesFromCSV(csvFile);

        System.out.println("Total zip codes read: " + zipCodes.size());

        Set<String> usedStartingPostalCodes = new HashSet<>();
        Random rand = new Random();

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputCsvFile))) {
            while (usedStartingPostalCodes.size() < zipCodes.size()) {
                String startingPostalCode;
                do {
                    startingPostalCode = zipCodes.get(rand.nextInt(zipCodes.size()));
                } while (usedStartingPostalCodes.contains(startingPostalCode));

                usedStartingPostalCodes.add(startingPostalCode);

                System.out.println("Starting postal code: " + startingPostalCode);

                for (int j = 0; j < zipCodes.size(); j++) {
                    if (!startingPostalCode.equals(zipCodes.get(j))) {
                        System.out.println("Checking route from " + startingPostalCode + " to " + zipCodes.get(j));
                     if (generateRoute(startingPostalCode, zipCodes.get(j))) {
                            System.out.println("Valid route found: " + startingPostalCode + "," + zipCodes.get(j));
                            pw.println(startingPostalCode + "," + zipCodes.get(j));
                        } else {
                            System.out.println("No valid route found: " + startingPostalCode + " to " + zipCodes.get(j));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
