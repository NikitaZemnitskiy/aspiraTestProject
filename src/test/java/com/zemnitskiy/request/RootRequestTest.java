package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.basemodel.*;
import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.RootResult;
import com.zemnitskiy.model.result.RunnerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zemnitskiy.Main.LEAGUE_COUNT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link RootRequest} class.
 */
@ExtendWith(MockitoExtension.class)
class RootRequestTest {

    @Mock
    private LeonApiClient apiClient;

    @InjectMocks
    private RootRequest rootRequest;

    private List<String> sportsNames;

    @BeforeEach
    public void setUp() {
        sportsNames = List.of("Football", "Basketball");
        rootRequest = new RootRequest(apiClient, sportsNames);
    }

    @Test
     void testFetch_SuccessfulResponse() {
        // Arrange
        Runner runner1 = new Runner(1L, "Team A Win", "1.80");
        Runner runner2 = new Runner(2L, "Draw", "3.50");
        Runner runner3 = new Runner(3L, "Team B Win", "2.00");

        Market market1 = new Market(100L, "Match Outcome", List.of(runner1, runner2, runner3));

        Event event1 = new Event(10L, "Team A vs Team B", System.currentTimeMillis() + 3600000, List.of(market1));

        League league1 = new League(1000L, "Premier League", 1, true, 1, List.of(event1));
        League league2 = new League(1001L, "Championship", 2, true, 2, List.of(event1));

        Region region1 = new Region("England", List.of(league1, league2));

        Sport sport1 = new Sport(1L, "Football", List.of(region1));
        Sport sport2 = new Sport(2L, "Basketball", List.of(region1));
        Sport sport3 = new Sport(3L, "Tennis", List.of(region1));

        List<Sport> sports = List.of(sport1, sport2, sport3);

        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.completedFuture(sports));

        when(apiClient.fetchEventsForLeague(any(League.class)))
                .thenAnswer(invocation -> {
                    League league = invocation.getArgument(0);
                    return CompletableFuture.completedFuture(league);
                });

        when(apiClient.fetchEventDetails(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(event1));

        // Act
        CompletableFuture<RootResult> futureResult = rootRequest.fetch();
        RootResult rootResult = futureResult.join();

        // Assert
        assertNotNull(rootResult, "RootResult should not be null");

        int expectedLeagueResultsCount = sportsNames.size() * LEAGUE_COUNT;
        assertEquals(expectedLeagueResultsCount, rootResult.leagueResults().size(),
                "Should have " + expectedLeagueResultsCount + " league results");

        // Verify the contents of leagueResults
        for (LeagueResult leagueResult : rootResult.leagueResults()) {
            assertNotNull(leagueResult, "LeagueResult should not be null");
            assertTrue(sportsNames.contains(leagueResult.sportName()), "Sport name should be in sportsNames");
            assertNotNull(leagueResult.league(), "League should not be null");
            assertTrue(leagueResult.league().top(), "League should be a top league");
            assertNotNull(leagueResult.matchResults(), "MatchResults should not be null");

            for (MatchResult matchResult : leagueResult.matchResults()) {
                assertNotNull(matchResult, "MatchResult should not be null");
                assertNotNull(matchResult.event(), "Event should not be null");
                assertNotNull(matchResult.marketResults(), "MarketResults should not be null");

                for (MarketResult marketResult : matchResult.marketResults()) {
                    assertNotNull(marketResult, "MarketResult should not be null");
                    assertNotNull(marketResult.market(), "Market should not be null");
                    assertNotNull(marketResult.runnerResults(), "RunnerResults should not be null");

                    for (RunnerResult runnerResult : marketResult.runnerResults()) {
                        assertNotNull(runnerResult, "RunnerResult should not be null");
                        assertNotNull(runnerResult.runner(), "Runner should not be null");
                    }
                }
            }
        }
    }

    @Test
     void testFetch_ApiClientThrowsException() {
        // Arrange
        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));

        // Act
        CompletableFuture<RootResult> futureResult = rootRequest.fetch();

        // Assert
        assertThrows(RuntimeException.class, futureResult::join, "Should throw RuntimeException when API client fails");
    }

    @Test
     void testFetch_NoMatchingSports() {
        // Arrange
        sportsNames = List.of("Cricket", "Hockey");
        rootRequest = new RootRequest(apiClient, sportsNames);

        Sport sport1 = new Sport(1L, "Football", List.of());
        Sport sport2 = new Sport(2L, "Basketball", List.of());
        List<Sport> sports = List.of(sport1, sport2);

        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.completedFuture(sports));

        // Act
        CompletableFuture<RootResult> futureResult = rootRequest.fetch();
        RootResult rootResult = futureResult.join();

        // Assert
        assertNotNull(rootResult, "RootResult should not be null");
        assertEquals(0, rootResult.leagueResults().size(), "Should have zero league results");
    }

    @Test
     void testFetch_NoTopLeagues() {
        // Arrange
        League league1 = new League(1000L, "League 1", 1, false, 1, List.of());
        Region region1 = new Region("Region 1", List.of(league1));
        Sport sport1 = new Sport(1L, "Football", List.of(region1));
        List<Sport> sports = List.of(sport1);

        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.completedFuture(sports));

        // Act
        CompletableFuture<RootResult> futureResult = rootRequest.fetch();
        RootResult rootResult = futureResult.join();

        // Assert
        assertNotNull(rootResult, "RootResult should not be null");
        assertEquals(0, rootResult.leagueResults().size(), "Should have zero league results");
    }

    @Test
     void testFetch_LeagueRequestThrowsException() {
        // Arrange
        League league1 = new League(1000L, "League 1", 1, true, 1, List.of());
        Region region1 = new Region("Region 1", List.of(league1));
        Sport sport1 = new Sport(1L, "Football", List.of(region1));
        List<Sport> sports = List.of(sport1);

        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.completedFuture(sports));
        when(apiClient.fetchEventsForLeague(league1)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("League API error")));

        // Act
        CompletableFuture<RootResult> futureResult = rootRequest.fetch();

        // Assert
        assertThrows(RuntimeException.class, futureResult::join, "Should throw RuntimeException when fetching league events fails");
    }
}