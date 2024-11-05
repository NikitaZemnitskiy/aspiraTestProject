package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.RootResult;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.Main.LEAGUE_COUNT;

/**
 * Represents an asynchronous request to retrieve and process league information for specified sports.
 *
 * <p>This class utilizes {@link LeonApiClient} to fetch base information about sports, filters the sports
 * based on provided names, retrieves top leagues for each filtered sport, and aggregates the results
 * into a {@link RootResult}.</p>
 *
 * @param apiClient   the API client used to communicate with the Leonbets API
 * @param sportsNames a list of sport names to filter and process
 * @see AsyncRequest
 * @see RootResult
 */
public record RootRequest(LeonApiClient apiClient, List<String> sportsNames) implements AsyncRequest<RootResult> {

    /**
     * Executes the asynchronous fetch operation to retrieve league information for the specified sports.
     *
     * <p>The method performs the following steps:
     * <ul>
     *   <li>Fetches base information about all sports.</li>
     *   <li>Filters the sports based on the provided {@code sportsNames} list.</li>
     *   <li>For each filtered sport, retrieves top leagues sorted by {@code topOrder} and limited by {@code LEAGUE_COUNT}.</li>
     *   <li>Aggregates the fetched {@link LeagueResult} instances into a {@link RootResult}.</li>
     * </ul>
     * </p>
     *
     * @return a {@link CompletableFuture} that completes with a {@link RootResult} containing the aggregated league results
     * @throws RuntimeException if the API call fails or returns invalid data
     */
    @Override
    public CompletableFuture<RootResult> fetch() {
        return apiClient.fetchBaseInformation()
                .thenCompose(sports -> {
                    List<CompletableFuture<LeagueResult>> leagueFutures = sports.stream()
                            .filter(sport -> sportsNames.contains(sport.name()))
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
