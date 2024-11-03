package com.zemnitskiy;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.parser.LeonParser;
import org.junit.jupiter.api.Test;

import static com.zemnitskiy.parser.LeonParser.BASE_URL;
import static org.junit.jupiter.api.Assertions.*;

class LeonParserLoadTest {

    @Test
    public void testProcessDataLoad() {
        final int NUMBER_OF_RUNS = 10;
        long totalDuration = 0;

        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            LeonApiClient apiClient = new LeonApiClient(BASE_URL);
            DisplayService displayService = new DisplayService();
            LeonParser parser = new LeonParser(apiClient, displayService);
            long startTime = System.nanoTime();
            parser.processData();
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalDuration += duration;

            System.out.println("Run " + (i + 1) + ": " + duration / 1_000_000 + " ms");
        }

        double averageDuration = totalDuration / (double) NUMBER_OF_RUNS;
        System.out.printf("Average processing time over %d runs: %.2f ms%n", NUMBER_OF_RUNS, averageDuration / 1_000_000);

        assertTrue(averageDuration < 5_000_000_000.0, "Average processing time exceeds acceptable limits");
    }
    //19.24 - Average processing time over 100 runs: 2073,84 ms
    //19.34 - Average processing time over 100 runs: 1431,38 ms
    //21.35 - Average processing time over 100 runs: 1010,07 ms

    //21.59 - Average processing time over 10 runs: 1680,27 ms

}