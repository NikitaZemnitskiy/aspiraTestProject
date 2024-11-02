package com.zemnitskiy;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Market;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.SportsResponse;
import com.zemnitskiy.service.LeonApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class LeonParser {

    private static final String BASE_URL = "https://leonbets.com/api-2/";
    private final static List<String> currentDiscipline = List.of("Football", "Tennis", "Ice Hockey", "Basketball");
    private final static Map<String, Integer> countryPriority = Map.of(
            "Europe", 0,
            "England", 1,
            "France", 2,
            "Germany", 3,
            "Italy", 4,
            "Spain", 5
    );

    public static void main(String[] args) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LeonApiService apiService = retrofit.create(LeonApiService.class);

        Call<List<Sport>> call = apiService.getBaseInformation("en-US", "urlv2");

        try {
            Response<List<Sport>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                List<League> leagues = response.body().stream()
                        .filter(sport -> currentDiscipline.contains(sport.getName()))
                        .flatMap(sport -> sport.getRegions().stream())
                        .peek(region -> {
                            List<League> filteredLeagues = region.getLeagues().stream()
                                    .filter(league -> league.getWeight() >= 999)
                                    .collect(toList());
                            region.setLeagues(filteredLeagues);
                        })
                        .sorted(Comparator.comparingInt(region -> countryPriority.getOrDefault(region.getName(), Integer.MAX_VALUE)))
                        .flatMap(region -> region.getLeagues().stream())
                        .limit(2)
                        .toList();

                Response<SportsResponse> sportResponse = apiService
                        .getSportResponse("en-US", leagues.getFirst().getId(), true, "reg,urlv2,mm2,rrc,nodup")
                        .execute();

                if (sportResponse.isSuccessful()) {
                    List<Event> events = sportResponse.body().getEvents().stream().limit(2).toList();
                    for (Event leagueEvent : events) {
                        Response<Event> eventResponse = apiService
                                .getEvent(leagueEvent.getId(), "en-US", true, "reg,urlv2,mm2,rrc,nodup")
                                .execute();
                        if (eventResponse.isSuccessful()) {
                            Event event = eventResponse.body();
                            League league = event.getLeague();
                            Sport sport = league.getSport();
                            System.out.println(sport.getName() + ", " + league.getName());
                            System.out.println("         " + event.getName() + " " + new Date(event.getKickoff()) + ", " + event.getId());
                            event.getMarkets().forEach(market -> {
                                System.out.println("         " + "         " +market.getName());
                                market.getRunners().forEach(runner -> {
                                            System.out.println("         " + "         " + "         " + runner.getName() + ", " + runner.getPriceStr() + ", " + runner.getId());
                                        }
                                );

                            });

                        } else {
                            System.out.println("Event Request failed: " + eventResponse.code());
                        }
                    }
                } else {
                    System.out.println("Sport Request failed: " + sportResponse.code());
                }

            } else {
                System.out.println("Request failed: " + response.code());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}