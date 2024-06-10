package com.bcs05.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is responsible for logging errors and handling exceptions.
 * It provides methods to log error messages and stack traces to a file,
 * and take appropriate actions based on the error type.
 */
public class ErrorHandler {

    public static void handleError(Exception e) {
        log("ERROR", e.getMessage(), e);
        takeAction(e);
    }

    public static void handleError(String message) {
        log("ERROR", message, null);
        takeAction(new Exception(message));
    }

    public static void logWarning(String message) {
        log("WARNING", message, null);
    }

    public static void logInfo(String message) {
        log("INFO", message, null);
    }

    private static void log(String level, String message, Exception e) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] %s: %s", timestamp, level, message);

        // Log to console
        if (level.equals("ERROR")) {
            System.err.println(logMessage);
        } else {
            System.out.println(logMessage);
        }

        if (e != null) {
            e.printStackTrace();
        }

        // Log to file
        try (FileWriter fw = new FileWriter("error_log.txt", true)) {
            fw.write(logMessage + "\n");
            if (e != null) {
                for (StackTraceElement element : e.getStackTrace()) {
                    fw.write(element.toString() + "\n");
                }
            }
            fw.write("\n");
        } catch (IOException ioe) {
            System.err.println("Error logging message: " + ioe.getMessage());
        }
    }

    private static void takeAction(Exception e) {
        // Determine action based on exception type or message
        if (e instanceof IOException) {
            // Retry the operation or provide a fallback
            System.err.println("An IOException occurred. Retrying operation...");
            // Retry logic here (simplified for demonstration)
            try {
                Thread.sleep(1000); // Simulate a retry delay
                // Retry the operation
            } catch (InterruptedException ie) {
                System.err.println("Retry interrupted: " + ie.getMessage());
            }
        } else {
            // Default action: notify the user or escalate the issue
            System.err.println("An unexpected error occurred: " + e.getMessage());
            // Here you could send an alert, escalate the issue, etc.
        }
    }
}
