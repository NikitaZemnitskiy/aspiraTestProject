package com.zemnitskiy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeonParserLoadTest {

    @Test
    public void testProcessDataLoad() {
        final int NUMBER_OF_RUNS = 100;
        long totalDuration = 0;

        for (int i = 0; i < NUMBER_OF_RUNS; i++) {
            LeonParser parser = new LeonParser();
            long startTime = System.nanoTime();
            parser.processData();
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalDuration += duration;

            System.out.println("Run " + (i + 1) + ": " + duration / 1_000_000 + " ms");
        }

        double averageDuration = totalDuration / (double) NUMBER_OF_RUNS;
        System.out.printf("Average processing time over %d runs: %.2f ms%n", NUMBER_OF_RUNS, averageDuration / 1_000_000);

        // Проверяем, что среднее время выполнения меньше 5000 мс (примерное значение)
        assertTrue(averageDuration < 5_000_000_000.0, "Average processing time exceeds acceptable limits");
    }
    //19.24 - Average processing time over 100 runs: 2073,84 ms
    //19.34 - Average processing time over 100 runs: 1431,38 ms
}