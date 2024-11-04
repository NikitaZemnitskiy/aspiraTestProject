package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.basemodel.Sport;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.RootResult;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.parser.LeonParser.LEAGUE_COUNT;

public record RootRequest(LeonApiClient apiClient, List<String> sportsNames) implements AsyncRequest<RootResult> {

    @Override
    public CompletableFuture<RootResult> fetch() {
        return apiClient.fetchBaseInformation()
                .thenCompose(sports -> {
                    List<Sport> filteredSports = sports.stream()
                            .filter(sport -> sportsNames.contains(sport.name()))
                            .toList();

                    List<CompletableFuture<LeagueResult>> leagueFutures = filteredSports.stream()
                            .flatMap(sport -> sport.regions().stream()
                                    .flatMap(region -> region.leagues().stream())
                                    .filter(League::top)
                                    .sorted(Comparator.comparingInt(League::topOrder))
                                    .limit(LEAGUE_COUNT)
                                    .map(league -> new LeagueRequest(apiClient, league, sport.name()).fetch())
                            )
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