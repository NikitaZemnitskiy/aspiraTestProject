package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.LeagueResult;
import com.zemnitskiy.model.Region;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.util.ComparatorUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

            sportsFuture.thenAccept(sports -> {
                for (String sportName : CURRENT_DISCIPLINES) {
                    List<League> leagues = filterRelevantLeagues(sports, sportName);

                    if (!leagues.isEmpty()) {
                        System.out.println("Processing sport: " + sportName);

                        List<CompletableFuture<LeagueResult>> leagueFutures = new ArrayList<>();

                        for (League league : leagues) {
                            CompletableFuture<LeagueResult> future;
                            try {
                                future = processLeague(league);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            leagueFutures.add(future);
                        }

                        CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                                .thenRun(() -> {
                                    leagueFutures.forEach(leagueFuture -> {
                                        try {
                                            LeagueResult leagueResult = leagueFuture.get();
                                            displayService.displayLeagueInfo(leagueResult.getLeague());
                                            leagueResult.getEvents().forEach(displayService::displayEvent);
                                        } catch (InterruptedException | ExecutionException e) {
                                            System.err.println("Error processing league result: " + e.getMessage());
                                        }
                                    });
                                }).join();
                    } else {
                        System.out.println("No relevant leagues found for sport: " + sportName);
                    }
                }
            }).join();
        } catch (Exception e) {
            System.err.println("Error during processing: " + e.getMessage());
        }
    }

     CompletableFuture<LeagueResult> processLeague(League league) throws IOException {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(events -> {
                    if (!events.isEmpty()) {
                        List<CompletableFuture<Event>> eventFutures = new ArrayList<>();

                        for (Event event : events) {
                            CompletableFuture<Event> future = processEvent(event);
                            eventFutures.add(future);
                        }

                        return CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> {
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
        List<League> leagues = sports.stream()
                .filter(sport -> sportName.equals(sport.name()))
                .flatMap(sport -> sport.regions().stream())
                .flatMap(region -> region.leagues().stream()
                        .peek(league -> league.setSportName(sportName))
                )
                .sorted((l1, l2) -> Integer.compare(l2.getWeight(), l1.getWeight()))
                .toList();

        if (leagues.isEmpty()) {
            return Collections.emptyList();
        }

        int maxWeight = leagues.stream()
                .mapToInt(League::getWeight)
                .findFirst()
                .orElse(0);

        int secondMaxWeight = leagues.stream()
                .mapToInt(League::getWeight)
                .filter(weight -> weight < maxWeight)
                .findFirst()
                .orElse(maxWeight);

        List<League> topLeagues = leagues.stream()
                .filter(league -> league.getWeight() == maxWeight || league.getWeight() == secondMaxWeight)
                .toList();

        List<Region> regions = new ArrayList<>(sports.stream()
                .filter(sport -> sportName.equals(sport.name()))
                .flatMap(sport -> sport.regions().stream())
                .toList());

        for (Region region : regions) {
            region.leagues().retainAll(topLeagues);
        }

        regions.sort(ComparatorUtils.getRegionComparator(sportName));

        return regions.stream()
                .flatMap(region -> region.leagues().stream())
                .sorted(ComparatorUtils.getLeagueComparator(sportName))
                .limit(2)
                .toList();
    }
}
