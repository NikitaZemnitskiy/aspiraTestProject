package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.RunnerResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record EventRequest(LeonApiClient apiClient, Event event) implements AsyncRequest<MatchResult> {

    @Override
    public CompletableFuture<MatchResult> fetch() {
        return apiClient.fetchEventDetails(event.id())
                .thenApply(markets -> {
                    List<MarketResult> marketResults = markets.markets().stream()
                            .map(market -> new MarketResult(
                                    market,
                                    market.runners().stream()
                                            .map(RunnerResult::new)
                                            .toList()
                            ))
                            .toList();
                    return new MatchResult(event, marketResults);
                });
    }
}