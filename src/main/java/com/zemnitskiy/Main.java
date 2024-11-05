package com.zemnitskiy;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.parser.LeonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final String FOOTBALL = "Football";
    public static final String TENNIS = "Tennis";
    public static final String ICE_HOCKEY = "Ice Hockey";
    public static final String BASKETBALL = "Basketball";

    public static final String BASE_URL = "https://leonbets.com/api-2/";
    public static final String LOCALE = "en-US";
    public static final String PARAMETERS = "reg,urlv2,mm2,rrc,nodup";
    public static final int LEAGUE_COUNT = 1;
    public static final int MATCH_COUNT = 2;
    public static final List<String> CURRENT_DISCIPLINES = List.of(
            FOOTBALL, TENNIS, ICE_HOCKEY, BASKETBALL
    );
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(3);
             HttpClient httpClient = HttpClient.newBuilder()
                     .executor(executorService)
                     .build()) {
            LeonApiClient apiClient = new LeonApiClient(httpClient, BASE_URL);
            LeonParser parser = new LeonParser(apiClient);
            parser.processData();
        } catch (Exception e) {
            logger.error("Error during processing: {}", e.getMessage(), e);
        }
    }
}
