package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.result.EventResult;
import com.zemnitskiy.model.result.LeagueResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.parser.LeonParser.MATCH_COUNT;

public record LeagueRequest(LeonApiClient apiClient, League league) implements AsyncRequest<LeagueResult> {

    @Override
    public CompletableFuture<LeagueResult> fetch() {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(events -> {
                    List<CompletableFuture<EventResult>> eventFutures = events.stream()
                            .map(event -> new EventRequest(apiClient, event).fetch())
                            .limit(MATCH_COUNT)
                            .toList();

                    return CompletableFuture.allOf(eventFutures.toArray(new CompletableFuture[0]))
                            .thenApply(_ -> {
                                List<EventResult> eventResults = eventFutures.stream()
                                        .map(CompletableFuture::join)
                                        .toList();
                                return new LeagueResult(league, eventResults);
                            });
                });
    }
}