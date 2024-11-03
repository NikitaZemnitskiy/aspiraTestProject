package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.result.SportResult;
import com.zemnitskiy.comparator.LeonComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LeonParser {

    private static final Logger logger = LoggerFactory.getLogger(LeonParser.class);

    public static final String FOOTBALL = "Football";
    public static final String TENNIS = "Tennis";
    public static final String ICE_HOCKEY = "Ice Hockey";
    public static final String BASKETBALL = "Basketball";

    private static final List<String> CURRENT_DISCIPLINES = List.of(
            FOOTBALL, TENNIS, ICE_HOCKEY, BASKETBALL
    );

    private final LeonApiClient apiClient;
    private final DisplayService displayService;

    public LeonParser(LeonApiClient apiClient, DisplayService displayService) {
        this.apiClient = apiClient;
        this.displayService = displayService;
    }

    public static void main(String[] args) {
        try (LeonApiClient apiClient = new LeonApiClient()) {
            DisplayService displayService = new DisplayService();
            LeonParser parser = new LeonParser(apiClient, displayService);
            parser.processData();
        } catch (Exception e) {
            logger.error("Error during processing: {}", e.getMessage(), e);
        }
    }

    public void processData() {
        apiClient.fetchBaseInformation()
                .thenCompose(sports -> {
                    List<CompletableFuture<SportResult>> sportFutures = CURRENT_DISCIPLINES.stream()
                            .map(sportName -> processSport(sports, sportName))
                            .toList();

                    return CompletableFuture.allOf(sportFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> sportFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                })
                .thenAccept(sportResults -> sportResults.forEach(this::displaySportResult))
                .exceptionally(e -> {
                    logger.error("Error during processing: {}", e.getMessage(), e);
                    return null;
                }).join();
    }

    private CompletableFuture<SportResult> processSport(List<Sport> sports, String sportName) {
        List<League> leagues = filterRelevantLeagues(sports, sportName);

        if (leagues.isEmpty()) {
            return CompletableFuture.completedFuture(new SportResult(sportName, Collections.emptyList()));
        }

        List<CompletableFuture<LeagueResult>> leagueFutures = leagues.stream()
                .map(this::processLeague)
                .toList();

        return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> {
                    List<LeagueResult> leagueResults = leagueFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                    return new SportResult(sportName, leagueResults);
                });
    }

    private CompletableFuture<LeagueResult> processLeague(League league) {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(events -> {
                    if (events.isEmpty()) {
                        return CompletableFuture.completedFuture(new LeagueResult(league, Collections.emptyList()));
                    }

                    List<CompletableFuture<Event>> eventFutures = events.stream()
                            .map(this::processEvent)
                            .toList();

                    return CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> {
                                List<Event> detailedEvents = eventFutures.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList());
                                return new LeagueResult(league, detailedEvents);
                            });
                });
    }

    private CompletableFuture<Event> processEvent(Event event) {
        return apiClient.fetchEventDetails(event.getId())
                .exceptionally(e -> {
                    logger.error("Error processing event {}: {}", event.getId(), e.getMessage(), e);
                    return event;
                });
    }

    private List<League> filterRelevantLeagues(List<Sport> sports, String sportName) {
        Comparator<League> leagueComparator = Comparator.comparingInt(League::weight).reversed()
                .thenComparing(LeonComparator.getLeagueComparator(sportName));

        return sports.stream()
                .filter(sport -> sportName.equals(sport.name()))
                .flatMap(sport -> sport.regions().stream())
                .flatMap(region -> region.leagues().stream())
                .sorted(leagueComparator)
                .limit(2)
                .collect(Collectors.toList());
    }

    private void displaySportResult(SportResult sportResult) {
        if (!sportResult.leagueResults().isEmpty()) {
            sportResult.leagueResults().forEach(leagueResult -> {
                displayService.displaySportAndLeagueInfo(sportResult.sportName(), leagueResult.league());
                leagueResult.events().forEach(displayService::displayEvent);
            });
        } else {
            logger.info("No relevant leagues found for sport: {}", sportResult.sportName());
        }
    }
}