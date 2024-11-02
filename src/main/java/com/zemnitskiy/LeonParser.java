package com.zemnitskiy;

import com.zemnitskiy.model.*;
import com.zemnitskiy.service.LeonApiService;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LeonParser {

    private static final String BASE_URL = "https://leonbets.com/api-2/";
    private static final String LOCALE = "en-US";
    private static final String PARAMETERS = "reg,urlv2,mm2,rrc,nodup";
    private static final List<String> CURRENT_DISCIPLINES = List.of(
            "Football", "Tennis", "Ice Hockey", "Basketball"
    );

    private static final Map<String, Integer> FOOTBALL_COUNTRY_PRIORITY = Map.ofEntries(
            Map.entry("Europe", 0),
            Map.entry("England", 1),
            Map.entry("France", 2),
            Map.entry("Germany", 3),
            Map.entry("Italy", 4),
            Map.entry("Spain", 5)
    );

    private static final Map<String, Integer> ICE_HOCKEY_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("NHL", 0),
            Map.entry("KHL", 1),
            Map.entry("Liiga", 2),
            Map.entry("DEL", 3)
    );

    private static final Map<String, Integer> BASKETBALL_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("1970324836975913", 0),
            Map.entry("1970324836982310", 1),
            Map.entry("1970324836976051", 2),
            Map.entry("1970324836974725", 3)
    );

    private static final Map<String, Integer> TENNIS_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("Paris", 0),
            Map.entry("Hong Kong", 1),
            Map.entry("Jiujiang", 2),
            Map.entry("Merida", 3)
    );

    private static final String INDENT_LEAGUE = "";
    private static final String INDENT_EVENT = "    ";
    private static final String INDENT_MARKET = "        ";
    private static final String INDENT_RUNNER = "            ";

    private final LeonApiService apiService;

    public LeonParser() {
        this.apiService = createApiService();
    }

    public static void main(String[] args) {
        new LeonParser().processData();
    }

    private LeonApiService createApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(LeonApiService.class);
    }

    public void processData() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(3)) {

            List<Sport> sports = fetchBaseInformation();

            for (String sportName : CURRENT_DISCIPLINES) {
                List<League> leagues = filterRelevantLeagues(sports, sportName);

                if (!leagues.isEmpty()) {
                    System.out.println("Processing sport: " + sportName);

                    // Use CompletableFuture for asynchronous processing of leagues
                    List<CompletableFuture<Void>> leagueFutures = new ArrayList<>();

                    for (League league : leagues) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(
                                () -> processLeague(league, executorService),
                                executorService
                        );
                        leagueFutures.add(future);
                    }

                    // Wait for all league tasks to complete
                    CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0])).join();
                } else {
                    System.out.println("No relevant leagues found for sport: " + sportName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error during processing: " + e.getMessage());
        }
    }

    private void processLeague(League league, ExecutorService executorService) {
        try {
            List<Event> events = fetchEventsForLeague(league);
            if (!events.isEmpty()) {
                // Use CompletableFuture for asynchronous processing of events
                List<CompletableFuture<Void>> eventFutures = new ArrayList<>();

                for (Event event : events) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> processEvent(event),
                            executorService
                    );
                    eventFutures.add(future);
                }

                // Wait for all event tasks to complete
                CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0])).join();

                // Display league information after processing all events
                displayLeagueInfo(league);
                events.forEach(this::displayEvent);
            }
        } catch (Exception e) {
            System.err.println("Error processing league %s: %s".formatted(league.getName(), e.getMessage()));
        }
    }

    private void processEvent(Event event) {
        try {
            Event detailedEvent = fetchEventDetails(event.getId());
            if (detailedEvent != null) {
                // Update event information
                event.setMarkets(detailedEvent.getMarkets());
                event.setCompetitors(detailedEvent.getCompetitors());
                event.setName(detailedEvent.getName());
            }
        } catch (Exception e) {
            System.err.printf("Error processing event %d: %s%n", event.getId(), e.getMessage());
        }
    }

    private void displayLeagueInfo(League league) {
        String sportName = Optional.ofNullable(league.getSportName()).orElse("Unknown Sport");
        System.out.println(INDENT_LEAGUE + "Sport - %s, %s".formatted(sportName, league.getName()));
    }

    private void displayEvent(Event event) {
        if (event != null) {
            LocalDateTime kickoffTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getKickoff()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(INDENT_EVENT + "%s %s, %d".formatted(
                    event.getName(),
                    kickoffTime.format(formatter),
                    event.getId()
            ));
            displayMarkets(event.getMarkets());
        } else {
            System.out.println(INDENT_EVENT + "Event details are not available.");
        }
    }

    private void displayMarkets(List<Market> markets) {
        if (markets != null && !markets.isEmpty()) {
            markets.forEach(market -> {
                System.out.println(INDENT_MARKET + market.getName());
                market.getRunners().forEach(runner ->
                        System.out.println(INDENT_RUNNER + "%s, %s, %d".formatted(
                                runner.getName(),
                                runner.getPriceStr(),
                                runner.getId()
                        ))
                );
            });
        } else {
            System.out.println(INDENT_MARKET + "No markets available.");
        }
    }

    private List<Sport> fetchBaseInformation() throws IOException {
        Call<List<Sport>> call = apiService.getBaseInformation(LOCALE, "urlv2");
        Response<List<Sport>> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            System.err.println("Failed to fetch sports data. Response code: " + response.code());
            return Collections.emptyList();
        }
    }

    private List<League> filterRelevantLeagues(List<Sport> sports, String sportName) {
        List<League> leagues = sports.stream()
                .filter(sport -> sportName.equals(sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                .flatMap(region -> region.getLeagues().stream()
                        .peek(league -> league.setSportName(sportName))
                )
                .sorted(Comparator.comparingInt(League::getWeight).reversed())
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
                .filter(sport -> sportName.equals(sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                .toList());

        for (Region region : regions) {
            region.getLeagues().retainAll(topLeagues);
        }

        regions.sort(getRegionComparator(sportName));

        return regions.stream()
                .flatMap(region -> region.getLeagues().stream())
                .sorted(getLeagueComparator(sportName))
                .limit(2)
                .toList();
    }

    private List<Event> fetchEventsForLeague(League league) throws IOException {
        Call<SportsResponse> call = apiService.getSportResponse(
                LOCALE,
                league.getId(),
                true,
                PARAMETERS
        );
        Response<SportsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body().getEvents().stream().limit(2).toList();
        } else {
            System.err.printf(
                    "Failed to fetch events for league %s. Response code: %d%n", league.getName(),
                    response.code()
            );
            return Collections.emptyList();
        }
    }

    private Event fetchEventDetails(long eventId) throws IOException {
        Call<Event> call = apiService.getEvent(
                eventId,
                LOCALE,
                true,
                PARAMETERS
        );
        Response<Event> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            System.err.printf(
                    "Failed to fetch details for event %d. Response code: %d%n", eventId,
                    response.code()
            );
            return null;
        }
    }

    private Comparator<Region> getRegionComparator(String sportName) {
        return switch (sportName) {
            case "Football" ->
                    Comparator.comparingInt(region ->
                            FOOTBALL_COUNTRY_PRIORITY.getOrDefault(region.getName(), Integer.MAX_VALUE)
                    );
            default -> Comparator.comparingInt(_ -> 0);
        };
    }

    private Comparator<League> getLeagueComparator(String sportName) {
        return switch (sportName) {
            case "Tennis" ->
                    Comparator.comparingInt(league ->
                            TENNIS_LEAGUE_PRIORITY.getOrDefault(league.getName(), Integer.MAX_VALUE)
                    );
            case "Ice Hockey" ->
                    Comparator.comparingInt(league ->
                            ICE_HOCKEY_LEAGUE_PRIORITY.getOrDefault(league.getName(), Integer.MAX_VALUE)
                    );
            case "Basketball" ->
                    Comparator.comparingInt(league ->
                            BASKETBALL_LEAGUE_PRIORITY.getOrDefault(String.valueOf(league.getId()), Integer.MAX_VALUE)
                    );
            default -> Comparator.comparingInt(_ -> 0);
        };
    }
}
