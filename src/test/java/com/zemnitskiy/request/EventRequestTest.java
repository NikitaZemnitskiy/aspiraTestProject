package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.model.basemodel.Market;
import com.zemnitskiy.model.basemodel.Runner;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.RunnerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link EventRequest} class.
 */
@ExtendWith(MockitoExtension.class)
class EventRequestTest {

    @Mock
    private LeonApiClient apiClient;

    private Event event;

    @InjectMocks
    private EventRequest eventRequest;

    @BeforeEach
    public void setUp() {
        // Initialize the event with all fields
        long eventId = 12345L;
        String eventName = "Team A vs Team B";
        long kickoff = System.currentTimeMillis() + 3600000; // 1 hour from now
        List<Market> markets = List.of();

        event = new Event(eventId, eventName, kickoff, markets);
        eventRequest = new EventRequest(apiClient, event);
    }

    @Test
     void testFetch_SuccessfulResponse() {
        // Arrange
        long eventId = event.id();

        Runner runner1 = new Runner(1L, "Team A Win", "1.80");
        Runner runner2 = new Runner(2L, "Draw", "3.50");
        Runner runner3 = new Runner(3L, "Team B Win", "2.00");

        Market market = new Market(100L, "Match Outcome", List.of(runner1, runner2, runner3));

        // Create an Event with markets populated
        Event fetchedEvent = new Event(eventId, event.name(), event.kickoff(), List.of(market));

        when(apiClient.fetchEventDetails(eventId)).thenReturn(CompletableFuture.completedFuture(fetchedEvent));

        // Act
        CompletableFuture<MatchResult> futureResult = eventRequest.fetch();
        MatchResult matchResult = futureResult.join();

        // Assert
        assertNotNull(matchResult, "MatchResult should not be null");
        assertEquals(fetchedEvent, matchResult.event(), "Event should match the fetched event");
        assertEquals(1, matchResult.marketResults().size(), "Should have one market");

        MarketResult marketResult = matchResult.marketResults().getFirst();
        assertEquals(market, marketResult.market(), "Market should match the expected value");
        assertEquals(3, marketResult.runnerResults().size(), "Should have three runners");

        RunnerResult runnerResult1 = marketResult.runnerResults().get(0);
        RunnerResult runnerResult2 = marketResult.runnerResults().get(1);
        RunnerResult runnerResult3 = marketResult.runnerResults().get(2);

        assertEquals(runner1, runnerResult1.runner(), "First runner should match the expected value");
        assertEquals(runner2, runnerResult2.runner(), "Second runner should match the expected value");
        assertEquals(runner3, runnerResult3.runner(), "Third runner should match the expected value");
    }

    @Test
     void testFetch_ApiClientThrowsException() {
        // Arrange
        long eventId = event.id();
        CompletableFuture<Event> failedFuture = CompletableFuture.failedFuture(new RuntimeException("API error"));

        when(apiClient.fetchEventDetails(eventId)).thenReturn(failedFuture);

        // Act & Assert
        CompletableFuture<MatchResult> futureResult = eventRequest.fetch();
        assertThrows(RuntimeException.class, futureResult::join, "Should throw RuntimeException when API client fails");
    }

    @Test
     void testFetch_EventWithEmptyMarkets() {
        // Arrange
        long eventId = event.id();
        Event fetchedEvent = new Event(eventId, event.name(), event.kickoff(), List.of());

        when(apiClient.fetchEventDetails(eventId)).thenReturn(CompletableFuture.completedFuture(fetchedEvent));

        // Act
        CompletableFuture<MatchResult> futureResult = eventRequest.fetch();
        MatchResult matchResult = futureResult.join();

        // Assert
        assertNotNull(matchResult, "MatchResult should not be null");
        assertEquals(fetchedEvent, matchResult.event(), "Event should match the fetched event");
        assertEquals(0, matchResult.marketResults().size(), "Market results should be empty");
    }

    @Test
     void testFetch_NullEventResponse() {
        // Arrange
        long eventId = event.id();
        when(apiClient.fetchEventDetails(eventId)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<MatchResult> futureResult = eventRequest.fetch();

        // Assert
        CompletionException exception = assertThrows(CompletionException.class, futureResult::join,
                "Should throw CompletionException when response is null");
        assertInstanceOf(NullPointerException.class, exception.getCause(), "Cause of CompletionException should be NullPointerException");
    }
}