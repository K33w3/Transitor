package com.bcs05;

import com.bcs05.data.GTFSDataHandler;
import com.bcs05.engine.GTFSEngine;
import com.bcs05.util.Path;
import com.bcs05.data.PostalCodeReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Random;

public class GTFSEngineTest {

    private GTFSDataHandler gtfsDataHandler;
    private GTFSEngine engine;
    private PostalCodeReader postalCodeReader;

    @BeforeEach
    public void setup() {
        gtfsDataHandler = new GTFSDataHandler();
        engine = new GTFSEngine();
        postalCodeReader = new PostalCodeReader();
    }

    @Test
    public void testRandomPostalCodePath() {
        ArrayList<String> postalCodes = new ArrayList<>(postalCodeReader.getRecords().keySet());
        Random random = new Random();
        String fromPostalCode = postalCodes.get(random.nextInt(postalCodes.size()));
        String toPostalCode = postalCodes.get(random.nextInt(postalCodes.size()));

        System.out.println("From Postal Code: " + fromPostalCode);
        System.out.println("To Postal Code: " + toPostalCode);

        Path resultPath = engine.findShortestDirectPath(fromPostalCode, toPostalCode, 0.3);

        assertNotNull(resultPath, "The path should not be null");
        assertTrue(resultPath.getTime().toMinutes() >= 0, "The path time should be non-negative");
        System.out.println("Test path from " + fromPostalCode + " to " + toPostalCode + " takes " + resultPath.getTime()
                + " minutes.");
    }
}
