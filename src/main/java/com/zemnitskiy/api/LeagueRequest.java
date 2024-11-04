package com.zemnitskiy.api;

import com.zemnitskiy.model.League;
import com.zemnitskiy.model.result.EventResult;
import com.zemnitskiy.model.result.LeagueResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeagueRequest implements AsyncRequest<LeagueResult> {
    private final LeonApiClient apiClient;
    private final League league;

    public LeagueRequest(LeonApiClient apiClient, League league) {
        this.apiClient = apiClient;
        this.league = league;
    }

    @Override
    public CompletableFuture<LeagueResult> fetch() {
        return apiClient.fetchEventsForLeague(league)
                .thenCompose(events -> {
                    List<CompletableFuture<EventResult>> eventFutures = events.stream()
                            .map(event -> new EventRequest(apiClient, event).fetch())
                            .limit(2) //?
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