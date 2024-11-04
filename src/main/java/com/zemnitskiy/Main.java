package com.zemnitskiy;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.parser.LeonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(3);
             HttpClient httpClient = HttpClient.newBuilder()
                     .executor(executorService)
                     .build()) {
            LeonApiClient apiClient = new LeonApiClient(httpClient);
            DisplayService displayService = new DisplayService();
            LeonParser parser = new LeonParser(apiClient, displayService);
            parser.processData();
        } catch (Exception e) {
            logger.error("Error during processing: {}", e.getMessage(), e);
        }
    }
}
