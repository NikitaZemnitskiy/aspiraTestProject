package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.result.SportResult;
import com.zemnitskiy.util.ComparatorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeonParser {

    private static final List<String> CURRENT_DISCIPLINES = List.of(
            "Football", "Tennis", "Ice Hockey", "Basketball"
    );

    private final LeonApiClient apiClient;
    private final DisplayService displayService;

    public LeonParser(LeonApiClient apiClient, DisplayService displayService) {
        this.apiClient = apiClient;
        this.displayService = displayService;
    }

    public static void main(String[] args) {
        LeonApiClient apiClient = new LeonApiClient();
        DisplayService displayService = new DisplayService();
        LeonParser parser = new LeonParser(apiClient, displayService);
        parser.processData();
        apiClient.shutdown();
    }

    public void processData() {
        try {
            CompletableFuture<List<Sport>> sportsFuture = apiClient.fetchBaseInformation();
            sportsFuture.thenCompose(sports -> {
                List<CompletableFuture<SportResult>> sportFutures = new ArrayList<>();
                for (String sportName : CURRENT_DISCIPLINES) {
                    CompletableFuture<SportResult> sportFuture;
                    sportFuture = processSport(sports, sportName);
                    sportFutures.add(sportFuture);
                }

                return CompletableFuture.allOf(sportFutures.toArray(new CompletableFuture[0]))
                        .thenApply(_ -> sportFutures.stream()
                                .map(CompletableFuture::join)
                                .toList());
            }).thenAccept(sportResults -> {
                for (SportResult sportResult : sportResults) {
                    if (!sportResult.leagueResults().isEmpty()) {
                        for (LeagueResult leagueResult : sportResult.leagueResults()) {
                            displayService.displaySportAndLeagueInfo(sportResult.sportName(), leagueResult.league());
                            leagueResult.events().forEach(displayService::displayEvent);
                        }
                    } else {
                        System.out.println("No relevant leagues found for sport: " + sportResult.sportName());
                    }
                }
            }).join();
        } catch (Exception e) {
            System.err.println("Error during processing: " + e.getMessage());
        }
    }

    private CompletableFuture<SportResult> processSport(List<Sport> sports, String sportName){
        List<League> leagues = filterRelevantLeagues(sports, sportName);

        if (!leagues.isEmpty()) {
            List<CompletableFuture<LeagueResult>> leagueFutures = new ArrayList<>();

            for (League league : leagues) {
                CompletableFuture<LeagueResult> future = processLeague(league);
                leagueFutures.add(future);
            }

            return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                    .thenApply(_ -> {
                        List<LeagueResult> leagueResults = leagueFutures.stream()
                                .map(CompletableFuture::join)
                                .toList();
                        return new SportResult(sportName, leagueResults);
                    });
        } else {
            return CompletableFuture.completedFuture(new SportResult(sportName, Collections.emptyList()));
        }
    }

    CompletableFuture<LeagueResult> processLeague(League league) {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(events -> {
                    if (!events.isEmpty()) {
                        List<CompletableFuture<Event>> eventFutures = events.stream()
                                .map(this::processEvent)
                                .toList();

                        return CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0]))
                                .thenApply(_ -> {
                                    List<Event> detailedEvents = eventFutures.stream()
                                            .map(CompletableFuture::join)
                                            .toList();
                                    return new LeagueResult(league, detailedEvents);
                                });
                    } else {
                        return CompletableFuture.completedFuture(new LeagueResult(league, Collections.emptyList()));
                    }
                });
    }

     CompletableFuture<Event> processEvent(Event event) {
        return apiClient.fetchEventDetails(event.getId())
                .thenApply(detailedEvent -> {
                    if (detailedEvent != null) {
                        event.setMarkets(detailedEvent.getMarkets());
                        event.setCompetitors(detailedEvent.getCompetitors());
                        event.setName(detailedEvent.getName());
                    }
                    return event;
                })
                .exceptionally(e -> {
                    System.err.printf("Error processing event %d: %s%n", event.getId(), e.getMessage());
                    return event;
                });
    }

    List<League> filterRelevantLeagues(List<Sport> sports, String sportName) {
        Comparator<League> leagueComparator = Comparator.comparingInt(League::weight).reversed()
                .thenComparing(ComparatorUtils.getLeagueComparator(sportName));

        return sports.stream()
                .filter(sport -> sportName.equals(sport.name()))
                .flatMap(sport -> sport.regions().stream())
                .flatMap(region -> region.leagues().stream())
                .sorted(leagueComparator)
                .limit(2)
                .toList();
    }
}
