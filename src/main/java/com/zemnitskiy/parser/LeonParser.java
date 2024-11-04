package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.request.RootRequest;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.result.LeagueResult;
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
                .thenAccept(rootResult -> rootResult.leagueResults()
                        .forEach(this::displayLeagueResult))
                .exceptionally(e -> {
                    logger.error("Error during processing: {}", e.getMessage(), e);
                    return null;
                }).join();
    }

    private void displayLeagueResult(LeagueResult leagueResult) {
        displayService.displaySportAndLeagueInfo(leagueResult.eventResults().getFirst().event().league().sport().name(), leagueResult.league());
        leagueResult.eventResults().forEach(eventResult -> displayService.displayEvent(eventResult.event()));
    }
}