package com.zemnitskiy.api;

import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.RootResult;
import com.zemnitskiy.parser.LeonParser;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RootRequest implements AsyncRequest<RootResult> {
    private final LeonApiClient apiClient;
    private final List<String> sportsNames;

    public RootRequest(LeonApiClient apiClient, List<String> sportsNames) {
        this.apiClient = apiClient;
        this.sportsNames = sportsNames;
    }

    @Override
    public CompletableFuture<RootResult> fetch() {
        return apiClient.fetchBaseInformation()
                .thenCompose(sports -> {
                    List<Sport> filteredSports = sports.stream()
                            .filter(sport -> sportsNames.contains(sport.name()))
                            .toList();

                    List<CompletableFuture<LeagueResult>> leagueFutures = filteredSports.stream()
                            .flatMap(sport -> sport.regions().stream())
                            .flatMap(region -> region.leagues().stream())
                            .filter(League::top)
                            .sorted(Comparator.comparingInt(League::topOrder))
                            .limit(LeonParser.LEAGUE_COUNT)
                            .map(league -> new LeagueRequest(apiClient, league).fetch())
                            .toList();

                    return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                            .thenApply(_ -> {
                                List<LeagueResult> leagueResults = leagueFutures.stream()
                                        .map(CompletableFuture::join)
                                        .toList();
                                return new RootResult(leagueResults);
                            });
                });
    }
}
