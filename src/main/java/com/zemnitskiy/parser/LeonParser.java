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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeonParser {

    public static final String BASE_URL = "https://leonbets.com/api-2/";
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
        LeonApiClient apiClient = new LeonApiClient(BASE_URL);
        DisplayService displayService = new DisplayService();
        LeonParser parser = new LeonParser(apiClient, displayService);
        parser.processData();
    }

    public void processData() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(3)) {

            List<Sport> sports = apiClient.fetchBaseInformation();

            for (String sportName : CURRENT_DISCIPLINES) {
                List<League> leagues = filterRelevantLeagues(sports, sportName);

                if (!leagues.isEmpty()) {
                    System.out.println("Processing sport: " + sportName);

                    List<CompletableFuture<LeagueResult>> leagueFutures = new ArrayList<>();

                    for (League league : leagues) {
                        CompletableFuture<LeagueResult> future = CompletableFuture.supplyAsync(
                                () -> processLeague(league, executorService),
                                executorService
                        );
                        leagueFutures.add(future);
                    }

                    List<LeagueResult> leagueResults = leagueFutures.stream()
                            .map(CompletableFuture::join)
                            .toList();

                    for (LeagueResult leagueResult : leagueResults) {
                        displayService.displayLeagueInfo(leagueResult.getLeague());
                        leagueResult.getEvents().forEach(displayService::displayEvent);
                    }
                } else {
                    System.out.println("No relevant leagues found for sport: " + sportName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error during processing: " + e.getMessage());
        }
    }

    private LeagueResult processLeague(League league, ExecutorService executorService) {
        try {
            List<Event> events = apiClient.fetchEventsForLeague(league);
            if (!events.isEmpty()) {
                List<CompletableFuture<Event>> eventFutures = new ArrayList<>();

                for (Event event : events) {
                    CompletableFuture<Event> future = CompletableFuture.supplyAsync(
                            () -> processEvent(event),
                            executorService
                    );
                    eventFutures.add(future);
                }

                List<Event> detailedEvents = eventFutures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                return new LeagueResult(league, detailedEvents);
            } else {
                return new LeagueResult(league, Collections.emptyList());
            }
        } catch (Exception e) {
            System.err.printf("Error processing league %s: %s%n", league.getName(), e.getMessage());
            return new LeagueResult(league, Collections.emptyList());
        }
    }

    private Event processEvent(Event event) {
        try {
            Event detailedEvent = apiClient.fetchEventDetails(event.getId());
            if (detailedEvent != null) {
                event.setMarkets(detailedEvent.getMarkets());
                event.setCompetitors(detailedEvent.getCompetitors());
                event.setName(detailedEvent.getName());
            }
            return event;
        } catch (Exception e) {
            System.err.printf("Error processing event %d: %s%n", event.getId(), e.getMessage());
            return event;
        }
    }

    private List<League> filterRelevantLeagues(List<Sport> sports, String sportName) {
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

        // Using findFirst() instead of get(0)
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

        // Creating a mutable list to avoid "immutable object is modified" warning
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
