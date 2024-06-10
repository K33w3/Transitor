package com.bcs05.api;

import com.bcs05.util.Coordinates;
import com.google.gson.Gson;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class APIClient {

    private static final Deque<Instant> requestTimestamps = new LinkedList<>();
    private static final AtomicInteger dailyRequestCount = new AtomicInteger(0);
    private static Instant lastRequestDay = Instant.now();

    /**
     * Checks the rate limit for making API requests.
     * 
     * This method checks the rate limit for making API requests and ensures that
     * the number of requests
     * made within specific time periods does not exceed the allowed limits. It also
     * keeps track of the
     * daily request count and resets it if a new day has started.
     * 
     * The implementation is inspired by Guillaume Laforge's "Client-side
     * consumption of a rate-limited API in Java"
     * As well as an article on Code Reliant. Sources can be found in the Readme
     * file.
     * 
     * @return true if the rate limit is not exceeded and a new request can be made,
     *         false otherwise.
     */
    public static synchronized boolean checkRateLimit() {
        Instant now = Instant.now();

        // Check and reset daily count if the day has passed
        if (now.isAfter(lastRequestDay.plusSeconds(86400))) {
            lastRequestDay = now;
            dailyRequestCount.set(0);
        }

        // Remove timestamps that are no longer in the rate limit period
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(now.minusSeconds(5))) {
            requestTimestamps.poll();
        }

        // Check rate limits for each time period
        if (!requestTimestamps.isEmpty() && ((dailyRequestCount.get() >= 100) ||
                (requestTimestamps.size() >= 1 && now.minusSeconds(5).isBefore(requestTimestamps.peekLast())) ||
                (requestTimestamps.stream().filter(timestamp -> timestamp.isAfter(now.minusSeconds(60))).count() >= 5)
                ||
                (requestTimestamps.stream().filter(timestamp -> timestamp.isAfter(now.minusSeconds(3600)))
                        .count() >= 40))) {
            return false;
        }

        // Record the new request timestamp and increment daily count
        requestTimestamps.offer(now);
        dailyRequestCount.incrementAndGet();
        return true;
    }

    public static Coordinates callAPI(String postcode) {
        // Check if rate limit allows for the call
        if (!checkRateLimit()) {
            System.out.println("Rate limit exceeded. Please try again later.");
            return null;
        }
        String json = "{\"postcode\": \"" + postcode + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://computerscience.dacs.unimaas.nl/get_coordinates"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) { // Check if the status code indicates success
                Gson gson = new Gson();
                Coordinates coordinates = gson.fromJson(response.body(), Coordinates.class);
                return coordinates;
            } else {
                switch (response.statusCode()) {
                    case 400:
                        System.out.println("The request was invalid.");
                        break;
                    case 404:
                        System.out.println("The requested postcode was not found.");
                        break;
                    case 429:
                        System.out.println("The rate limit was exceeded. Please try again later.");
                        break;
                    case 500:
                        System.out.println("An internal server error occurred. Failed to fetch coordinates.");
                        break;
                    default:
                        System.out.println("An unexpected error occurred.");
                }
                return null;
            }
        } catch (IOException e) {
            System.out.println("An IO exception occurred: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            System.out.println("The request was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
            return null;
        }
    }
}
