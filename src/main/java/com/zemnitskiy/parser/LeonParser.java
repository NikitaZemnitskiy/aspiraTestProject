package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.result.EventResult;
import com.zemnitskiy.model.result.RootResult;
import com.zemnitskiy.request.RootRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LeonParser {

    private static final Logger logger = LoggerFactory.getLogger(LeonParser.class);

    public static final String FOOTBALL = "Football";
    public static final String TENNIS = "Tennis";
    public static final String ICE_HOCKEY = "Ice Hockey";
    public static final String BASKETBALL = "Basketball";

    public static final String BASE_URL = "https://leonbets.com/api-2/";
    public static final String LOCALE = "en-US";
    public static final String PARAMETERS = "reg,urlv2,mm2,rrc,nodup";
    public static final int LEAGUE_COUNT = 1;
    public static final int MATCH_COUNT = 2;

    private static final List<String> CURRENT_DISCIPLINES = List.of(
            FOOTBALL, TENNIS, ICE_HOCKEY, BASKETBALL
    );

    private final LeonApiClient apiClient;
    private final DisplayService displayService;

    public LeonParser(LeonApiClient apiClient, DisplayService displayService) {
        this.apiClient = apiClient;
        this.displayService = displayService;
    }

    public void processData() {
        RootRequest rootRequest = new RootRequest(apiClient, CURRENT_DISCIPLINES);
        rootRequest.fetch()
                .thenAccept(rootResults -> rootResults.forEach(this::displayRootResult))
                .exceptionally(e -> {
                    logger.error("Error during processing: {}", e.getMessage(), e);
                    return null;
                }).join();
    }

    private void displayRootResult(RootResult rootResult) {
        rootResult.leagueResults()
                .forEach(leagueResult -> {
                            displayService.displaySportAndLeagueInfo(rootResult.sport().name(), leagueResult.league());
                            leagueResult.eventResults().stream()
                                    .map(EventResult::event)
                                    .forEach(displayService::displayEvent);
                        }
                );

    }

}