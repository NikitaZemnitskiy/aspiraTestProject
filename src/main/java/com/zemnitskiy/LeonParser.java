package com.zemnitskiy;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Market;
import com.zemnitskiy.model.Region;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.SportsResponse;
import com.zemnitskiy.service.LeonApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class LeonParser {

    private static final String BASE_URL = "https://leonbets.com/api-2/";
    private static final List<String> CURRENT_DISCIPLINES = List.of("Football", "Tennis", "Ice Hockey", "Basketball");
    private static final Map<String, Integer> FOOTBALL_COUNTRY_PRIORITY = Map.of(
            "Europe", 0, "England", 1, "France", 2, "Germany", 3, "Italy", 4, "Spain", 5
    );
    private static final Map<String, Integer> ICE_HOCKEY_LEAGUE_PRIORITY = Map.of(
            "NHL", 0, "KHL", 1, "Liiga", 2, "DEL", 3
    );
    private static final Map<String, Integer> BASKETBALL_LEAGUE_PRIORITY = Map.of(
            "1970324836975913", 0, "1970324836982310", 1, "1970324836976051", 2, "1970324836974725", 3
    );

    private static final Map<String, Integer> TENNIS_LEAGUE_PRIORITY = Map.of(
            "Paris", 0, "Hong Kong", 1, "Jiujiang", 2, "Merida", 3
    );

    private final LeonApiService apiService;

    public LeonParser() {
        this.apiService = createApiService();
    }

    public static void main(String[] args) {
        new LeonParser().processData();
        System.exit(0);
    }


    private LeonApiService createApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(LeonApiService.class);
    }

    public void processData() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<List<Event>>> futures = new ArrayList<>();

        try {
            List<Sport> sports = fetchBaseInformation();
            List<League> leagues = new ArrayList<>();

            for (String sportName : CURRENT_DISCIPLINES) {
                List<League> leagueList = filterRelevantLeagues(sports, sportName);
                leagues.addAll(leagueList);
            }

            if (!leagues.isEmpty()) {
                for (League league : leagues) {
                    futures.add(executorService.submit(() -> fetchEventsForLeague(league)));
                }

                for (Future<List<Event>> future : futures) {
                    try {
                        List<Event> events = future.get();
                        boolean isLeagueInfoPrinted = false;

                        for (Event event : events) {
                            isLeagueInfoPrinted = displayEventDetails(event, isLeagueInfoPrinted);
                        }
                    } catch (ExecutionException e) {
                        System.err.println("Error fetching events: " + e.getCause());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                System.out.println("No relevant leagues found.");
            }
        } catch (IOException e) {
            System.err.println("Error during processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private List<Sport> fetchBaseInformation() throws IOException {
        Call<List<Sport>> call = apiService.getBaseInformation("en-US", "urlv2");
        Response<List<Sport>> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            System.err.println("Failed to fetch sports data. Response code: " + response.code());
            return Collections.emptyList();
        }
    }

    private List<League> filterRelevantLeagues(List<Sport> sports, String sportName) {

        // Filter leagues by the sportName and sort them by weight in descending order
        List<League> leagues = sports.stream()
                .filter(sport -> Objects.equals(sportName, sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                .flatMap(region -> region.getLeagues().stream())
                .sorted(Comparator.comparingInt(League::getWeight).reversed())
                .toList();

        // Find the maximum weight and the second-highest weight
        int maxWeight = leagues.isEmpty() ? 0 : leagues.getFirst().getWeight();
        int secondMaxWeight = leagues.stream()
                .map(League::getWeight)
                .distinct()
                .filter(weight -> weight < maxWeight)
                .findFirst()
                .orElse(0);

        // Filter leagues based on the found weights
        List<League> topLeagues = new ArrayList<>(leagues.stream()
                .filter(league -> league.getWeight() == maxWeight)
                .toList());

        if (topLeagues.size() < 2) {
            topLeagues.addAll(leagues.stream().filter(league -> league.getWeight() == secondMaxWeight).toList());
        }

        return sports.stream()
                .filter(sport -> Objects.equals(sportName, sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                // Retain only the leagues that are in topLeagues for each region
                .peek(region -> region.getLeagues().retainAll(topLeagues))
                // Sort regions based on region priority
                .sorted(getRegionComparator(sportName))
                .flatMap(region -> region.getLeagues().stream())
                // Sort regions based on league priority
                .sorted(getLeagueComparator(sportName))
                .limit(2)
                .toList();
    }

    private List<Event> fetchEventsForLeague(League league) throws IOException {
        Call<SportsResponse> call = apiService.getSportResponse("en-US", league.getId(), true, "reg,urlv2,mm2,rrc,nodup");
        Response<SportsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body().getEvents().stream().limit(2).collect(Collectors.toList());
        } else {
            System.err.println("Failed to fetch events for league " + league.getName() +
                    ". Response code: " + response.code());
            return Collections.emptyList();
        }
    }

    private boolean displayEventDetails(Event event, boolean isLeagueInfoPrinted) throws IOException {
        Call<Event> call = apiService.getEvent(event.getId(), "en-US", true, "reg,urlv2,mm2,rrc,nodup");
        Response<Event> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            Event detailedEvent = response.body();
            if (!isLeagueInfoPrinted) {
                displayLeagueInfo(detailedEvent.getLeague());
                isLeagueInfoPrinted = true;
            }
            displayEvent(detailedEvent);
        } else {
            System.err.println("Failed to fetch details for event " + event.getId() +
                    ". Response code: " + response.code());
        }
        return isLeagueInfoPrinted;
    }

    private void displayLeagueInfo(League league) {
        Sport sport = league.getSport();
        System.out.println("Sport - " + sport.getName() + ", " + league.getName());
    }

    private void displayEvent(Event event) {
        System.out.println("    " + event.getName() + " " + new Date(event.getKickoff()) + ", " + event.getId());
        displayMarkets(event.getMarkets());
    }

    private void displayMarkets(List<Market> markets) {
        markets.forEach(market -> {
            System.out.println("        " + market.getName());
            market.getRunners().forEach(runner ->
                    System.out.println("            " + runner.getName() + ", " + runner.getPriceStr() + ", " + runner.getId())
            );
        });
    }

    private Comparator<Region> getRegionComparator(String sportName) {
        if ("Football".equals(sportName)) {
            return Comparator.comparingInt(region -> FOOTBALL_COUNTRY_PRIORITY.getOrDefault(region.getName(), Integer.MAX_VALUE));
        } else {
            return Comparator.comparingInt(_ -> 0);
        }
    }

    private Comparator<League> getLeagueComparator(String sportName) {
        return switch (sportName) {
            case "Tennis" ->
                    Comparator.comparingInt(league -> TENNIS_LEAGUE_PRIORITY.getOrDefault(league.getName(), Integer.MAX_VALUE));
            case "Ice Hockey" ->
                    Comparator.comparingInt(league -> ICE_HOCKEY_LEAGUE_PRIORITY.getOrDefault(league.getName(), Integer.MAX_VALUE));
            case "Basketball" ->
                    Comparator.comparingInt(league -> BASKETBALL_LEAGUE_PRIORITY.getOrDefault(String.valueOf(league.getId()), Integer.MAX_VALUE));
            case null, default -> Comparator.comparingInt(_ -> 0);
        };
    }

}