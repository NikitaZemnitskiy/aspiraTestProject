package com.zemnitskiy;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.parser.LeonParser;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class LeonParserLoadTest {

    @Test
     void testProcessDataLoad() {
        final int NUMBER_OF_RUNS = 100;
        long totalDuration = 0;
        try (ExecutorService executorService = Executors.newFixedThreadPool(3);
             HttpClient httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build()) {
            LeonApiClient apiClient = new LeonApiClient(httpClient);
            DisplayService displayService = new DisplayService();
            LeonParser parser = new LeonParser(apiClient, displayService);
            for (int i = 0; i < NUMBER_OF_RUNS; i++) {
                long startTime = System.nanoTime();
                parser.processData();
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                totalDuration += duration;
                System.out.println("Run " + (i + 1) + ": " + duration / 1_000_000 + " ms");
            }
        }

        double averageDuration = totalDuration / (double) NUMBER_OF_RUNS;
        System.out.printf("Average processing time over %d runs: %.2f ms%n", NUMBER_OF_RUNS, averageDuration / 1_000_000);

        assertTrue(averageDuration < 5_000_000_000.0, "Average processing time exceeds acceptable limits");
    }
    //19.24 - Average processing time over 100 runs: 2073,84 ms
    //19.34 - Average processing time over 100 runs: 1431,38 ms
    //21.35 - Average processing time over 100 runs: 1010,07 ms
    //21.59 - Average processing time over 10 runs: 1680,27 ms
    //0.29 - Average processing time over 10 runs: 351,72 ms



}