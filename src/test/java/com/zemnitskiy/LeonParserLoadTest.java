package com.zemnitskiy;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.parser.LeonParser;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zemnitskiy.Main.BASE_URL;
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
            LeonApiClient apiClient = new LeonApiClient(httpClient, BASE_URL);
            LeonParser parser = new LeonParser(apiClient);
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



}