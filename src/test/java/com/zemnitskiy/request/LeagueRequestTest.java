package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.basemodel.Market;
import com.zemnitskiy.model.basemodel.Runner;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.MarketResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueRequestTest {

    @Mock
    private LeonApiClient apiClient;

    private League league;
    private final String sportName = "Football";

    @InjectMocks
    private LeagueRequest leagueRequest;

    @BeforeEach
    public void setUp() {
        long leagueId = 1000L;
        String leagueName = "Premier League";
        int weight = 1;
        boolean top = true;
        int topOrder = 1;
        List<Event> events = List.of();

        league = new League(leagueId, leagueName, weight, top, topOrder, events);
        leagueRequest = new LeagueRequest(apiClient, league, sportName);
    }

    @Test
    void testFetch_SuccessfulResponse() {
        // Arrange
        Event event1 = new Event(1L, "Team A vs Team B", System.currentTimeMillis() + 3600000, List.of());
        Event event2 = new Event(2L, "Team C vs Team D", System.currentTimeMillis() + 7200000, List.of());
        List<Event> events = List.of(event1, event2);

        League updatedLeague = new League(league.id(), league.name(), league.weight(), league.top(), league.topOrder(), events);

        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.completedFuture(updatedLeague));

        Runner runner1 = new Runner(1L, "Team A Win", "1.80");
        Runner runner2 = new Runner(2L, "Draw", "3.50");
        Runner runner3 = new Runner(3L, "Team B Win", "2.00");
        Market market1 = new Market(100L, "Match Outcome", List.of(runner1, runner2, runner3));
        Event event1WithMarkets = new Event(event1.id(), event1.name(), event1.kickoff(), List.of(market1));

        when(apiClient.fetchEventDetails(event1.id())).thenReturn(CompletableFuture.completedFuture(event1WithMarkets));

        Runner runner4 = new Runner(4L, "Team C Win", "1.70");
        Runner runner5 = new Runner(5L, "Draw", "3.80");
        Runner runner6 = new Runner(6L, "Team D Win", "2.10");
        Market market2 = new Market(101L, "Match Outcome", List.of(runner4, runner5, runner6));
        Event event2WithMarkets = new Event(event2.id(), event2.name(), event2.kickoff(), List.of(market2));

        when(apiClient.fetchEventDetails(event2.id())).thenReturn(CompletableFuture.completedFuture(event2WithMarkets));

        // Act
        CompletableFuture<LeagueResult> futureResult = leagueRequest.fetch();
        LeagueResult leagueResult = futureResult.join();

        // Assert
        assertNotNull(leagueResult, "LeagueResult should not be null");
        assertEquals(sportName, leagueResult.sportName(), "Sport name should match");
        assertEquals(league, leagueResult.league(), "League should match");
        assertEquals(2, leagueResult.matchResults().size(), "Should have two match results");

        MatchResult matchResult1 = leagueResult.matchResults().getFirst();
        assertEquals(event1WithMarkets, matchResult1.event(), "Event should match the fetched event1");
        assertEquals(1, matchResult1.marketResults().size(), "Should have one market in matchResult1");

        MarketResult marketResult1 = matchResult1.marketResults().getFirst();
        assertEquals(market1, marketResult1.market(), "Market should match the expected market");
        assertEquals(3, marketResult1.runnerResults().size(), "Should have three runners in marketResult1");

        MatchResult matchResult2 = leagueResult.matchResults().get(1);
        assertEquals(event2WithMarkets, matchResult2.event(), "Event should match the fetched event2");
        assertEquals(1, matchResult2.marketResults().size(), "Should have one market in matchResult2");

        MarketResult marketResult2 = matchResult2.marketResults().getFirst();
        assertEquals(market2, marketResult2.market(), "Market should match the expected market");
        assertEquals(3, marketResult2.runnerResults().size(), "Should have three runners in marketResult2");
    }

    @Test
    void testFetch_ApiClientThrowsException() {
        // Arrange
        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));

        // Act
        CompletableFuture<LeagueResult> futureResult = leagueRequest.fetch();

        // Assert
        assertThrows(RuntimeException.class, futureResult::join, "Should throw RuntimeException when API client fails");
    }

    @Test
    void testFetch_EventRequestThrowsException() {
        // Arrange
        Event event1 = new Event(1L, "Team A vs Team B", System.currentTimeMillis() + 3600000, List.of());
        List<Event> events = List.of(event1);

        League updatedLeague = new League(league.id(), league.name(), league.weight(), league.top(), league.topOrder(), events);

        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.completedFuture(updatedLeague));

        when(apiClient.fetchEventDetails(event1.id())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Event API error")));

        // Act
        CompletableFuture<LeagueResult> futureResult = leagueRequest.fetch();

        // Assert
        assertThrows(RuntimeException.class, futureResult::join, "Should throw RuntimeException when fetching event details fails");
    }

    @Test
    void testFetch_NoEventsInLeague() {
        // Arrange
        League updatedLeague = new League(league.id(), league.name(), league.weight(), league.top(), league.topOrder(), List.of());

        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.completedFuture(updatedLeague));

        // Act
        CompletableFuture<LeagueResult> futureResult = leagueRequest.fetch();
        LeagueResult leagueResult = futureResult.join();

        // Assert
        assertNotNull(leagueResult, "LeagueResult should not be null");
        assertEquals(sportName, leagueResult.sportName(), "Sport name should match");
        assertEquals(league, leagueResult.league(), "League should match");
        assertEquals(0, leagueResult.matchResults().size(), "Should have zero match results");
    }
}