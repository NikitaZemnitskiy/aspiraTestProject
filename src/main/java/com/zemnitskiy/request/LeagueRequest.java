package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MatchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.Main.MATCH_COUNT;

/**
 * Represents an asynchronous request to retrieve detailed information about a specific league,
 * including a limited number of match results.
 *
 * <p>This class utilizes {@link LeonApiClient} to fetch events for the provided {@link League},
 * processes the events to obtain {@link MatchResult} instances, and constructs a {@link LeagueResult}
 * containing the sport name, league details, and match results.</p>
 *
 * @param apiClient the API client for interacting with the Leonbets API
 * @param league the league to fetch events for
 * @param sportName the name of the sport associated with the league
 */
public record LeagueRequest(LeonApiClient apiClient, League league, String sportName) implements AsyncRequest<LeagueResult> {

    /**
     * Executes the asynchronous fetch operation to retrieve league details and match results.
     *
     * @return a {@link CompletableFuture} that completes with a {@link LeagueResult}
     * @throws RuntimeException if the API call fails or returns invalid data
     */
    @Override
    public CompletableFuture<LeagueResult> fetch() {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(updatedLeague -> {
                    List<CompletableFuture<MatchResult>> matchFutures = updatedLeague.events().stream()
                            .map(event -> new EventRequest(apiClient, event).fetch())
                            .limit(MATCH_COUNT)
                            .toList();

                    return CompletableFuture.allOf(matchFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<MatchResult> matchResults = matchFutures.stream()
                                        .map(CompletableFuture::join)
                                        .toList();
                                return new LeagueResult(sportName, league, matchResults);
                            });
                });
    }
}
