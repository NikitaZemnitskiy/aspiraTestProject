package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.RunnerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an asynchronous request to retrieve detailed information about a specific event,
 * including associated markets and runners.
 *
 * <p>This class utilizes {@link LeonApiClient} to fetch event details, processes the markets
 * and runners associated with the event, and constructs a {@link MatchResult} containing
 * the event and its market results.</p>
 *
 * @param apiClient the API client used to communicate with the Leonbets API
 * @param event the event to fetch detailed information for
 * @see AsyncRequest
 * @see MatchResult
 */
public record EventRequest(LeonApiClient apiClient, Event event) implements AsyncRequest<MatchResult> {
    private static final Logger logger = LoggerFactory.getLogger(EventRequest.class);

    /**
     * Executes the asynchronous fetch operation to retrieve detailed information about the event.
     *
     * <p>The method performs the following steps:
     * <ul>
     *   <li>Fetches detailed information about the event using {@link LeonApiClient#fetchEventDetails(long)}.</li>
     *   <li>Processes each market within the event to create {@link MarketResult} instances.</li>
     *   <li>Processes each runner within the markets to create {@link RunnerResult} instances.</li>
     *   <li>Aggregates the market results into a {@link MatchResult} containing the event and its markets.</li>
     * </ul>
     * </p>
     *
     * @return a {@link CompletableFuture} that completes with a {@link MatchResult} containing the event details and associated market results
     * @throws RuntimeException if the API call fails or returns invalid data
     */
    @Override
    public CompletableFuture<MatchResult> fetch() {
        return apiClient.fetchEventDetails(event.id())
                .thenApply(fetchedEvent -> {
                    logger.debug("Fetched event: {}", event.id());
                    List<MarketResult> marketResults = fetchedEvent.markets().stream()
                            .map(market -> new MarketResult(
                                    market,
                                    market.runners().stream()
                                            .map(RunnerResult::new)
                                            .toList()
                            ))
                            .toList();
                    return new MatchResult(fetchedEvent, marketResults);
                });
    }
}
