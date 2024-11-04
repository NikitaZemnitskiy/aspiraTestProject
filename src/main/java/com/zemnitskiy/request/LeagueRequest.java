package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MatchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.parser.LeonParser.MATCH_COUNT;

public record LeagueRequest(LeonApiClient apiClient, League league, String sportName) implements AsyncRequest<LeagueResult> {

    @Override
    public CompletableFuture<LeagueResult> fetch() {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(l -> {
                    List<CompletableFuture<MatchResult>> matchFutures = l.events().stream()
                            .map(event -> new EventRequest(apiClient, event).fetch())
                            .limit(MATCH_COUNT)
                            .toList();

                    return CompletableFuture.allOf(matchFutures.toArray(new CompletableFuture[0]))
                            .thenApply(_ -> {
                                List<MatchResult> matchResults = matchFutures.stream()
                                        .map(CompletableFuture::join)
                                        .toList();
                                return new LeagueResult(sportName, league, matchResults);
                            });
                });
    }
}