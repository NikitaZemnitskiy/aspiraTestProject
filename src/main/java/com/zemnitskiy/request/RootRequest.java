package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.RootResult;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.parser.LeonParser.LEAGUE_COUNT;

public record RootRequest(LeonApiClient apiClient, List<String> sportsNames) implements AsyncRequest<List<RootResult>> {

    @Override
    public CompletableFuture<List<RootResult>> fetch() {
        return apiClient.fetchBaseInformation()
                .thenCompose(sports -> {
                    List<Sport> filteredSports = sports.stream()
                            .filter(sport -> sportsNames.contains(sport.name()))
                            .toList();

                    List<CompletableFuture<RootResult>> rootFutures = filteredSports.stream()
                            .map(sport -> {
                                List<CompletableFuture<LeagueResult>> leagueFutures = sport.regions().stream()
                                        .flatMap(region -> region.leagues().stream())
                                        .filter(League::top)
                                        .sorted(Comparator.comparingInt(League::topOrder))
                                        .limit(LEAGUE_COUNT)
                                        .map(league -> new LeagueRequest(apiClient, league).fetch())
                                        .toList();

                                return CompletableFuture.allOf(leagueFutures.toArray(new CompletableFuture[0]))
                                        .thenApply(_ -> {
                                            List<LeagueResult> leagueResults = leagueFutures.stream()
                                                    .map(CompletableFuture::join)
                                                    .toList();
                                            return new RootResult(sport, leagueResults);
                                        });
                            })
                            .toList();

                    return CompletableFuture.allOf(rootFutures.toArray(new CompletableFuture[0]))
                            .thenApply(_ -> rootFutures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                });
    }
}