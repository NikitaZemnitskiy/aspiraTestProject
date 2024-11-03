package com.zemnitskiy.parser;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.display.DisplayService;
import com.zemnitskiy.model.Competitor;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.LeagueResult;
import com.zemnitskiy.model.Market;
import com.zemnitskiy.model.Region;
import com.zemnitskiy.model.Sport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeonParserTest {

    private LeonApiClient apiClient;
    private DisplayService displayService;
    private LeonParser parser;

    @BeforeEach
    void setUp() {
        apiClient = mock(LeonApiClient.class);
        displayService = mock(DisplayService.class);
        parser = new LeonParser(apiClient, displayService);
        doNothing().when(displayService).displayEvent(any());
    }

    @Test
    void testFilterRelevantLeagues() {
        // Arrange
        String sportName = "Football";
        Sport footballSport = new Sport(
                1L,
                sportName,
                List.of(
                        new Region("England", new ArrayList<>(List.of(
                                new League(101L, "Premier League", 10, null),
                                new League(102L, "Championship", 8, null)
                        ))),
                        new Region("Spain", new ArrayList<>(List.of(
                                new League(201L, "La Liga", 9, null),
                                new League(202L, "Segunda Division", 7, null)
                        )))
                )
        );

        List<Sport> sports = List.of(footballSport);

        // Act
        List<League> result = parser.filterRelevantLeagues(sports, sportName);

        // Assert
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(201L, result.get(1).getId());
    }

    @Test
    void testProcessEvent() throws Exception {
        // Arrange
        Event initialEvent = new Event();
        initialEvent.setId(1001L);
        initialEvent.setName("Initial Event");

        Event detailedEvent = new Event();
        detailedEvent.setId(1001L);
        detailedEvent.setName("Detailed Event");
        detailedEvent.setMarkets(List.of(new Market()));
        detailedEvent.setCompetitors(List.of(new Competitor()));

        when(apiClient.fetchEventDetails(1001L)).thenReturn(CompletableFuture.completedFuture(detailedEvent));

        // Act
        CompletableFuture<Event> future = parser.processEvent(initialEvent);
        Event resultEvent = future.get();

        // Assert
        assertEquals("Detailed Event", resultEvent.getName());
        assertNotNull(resultEvent.getMarkets());
        assertNotNull(resultEvent.getCompetitors());
    }

    @Test
    void testProcessLeague() throws Exception {
        // Arrange
        League league = new League();
        league.setId(2001L);
        league.setName("Test League");

        Event event1 = new Event();
        event1.setId(3001L);

        Event event2 = new Event();
        event2.setId(3002L);

        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.completedFuture(List.of(event1, event2)));

        Event detailedEvent1 = new Event();
        detailedEvent1.setId(3001L);
        detailedEvent1.setName("Detailed Event 1");

        Event detailedEvent2 = new Event();
        detailedEvent2.setId(3002L);
        detailedEvent2.setName("Detailed Event 2");

        LeonParser spyParser = spy(parser);

        doReturn(CompletableFuture.completedFuture(detailedEvent1)).when(spyParser).processEvent(event1);
        doReturn(CompletableFuture.completedFuture(detailedEvent2)).when(spyParser).processEvent(event2);

        // Act
        CompletableFuture<LeagueResult> future = spyParser.processLeague(league);
        LeagueResult result = future.get();

        // Assert
        assertEquals(league, result.getLeague());
        assertEquals(2, result.getEvents().size());
        assertEquals("Detailed Event 1", result.getEvents().get(0).getName());
        assertEquals("Detailed Event 2", result.getEvents().get(1).getName());
    }

    @Test
    void testProcessData() throws Exception {
        // Arrange
        String sportName = "Football";
        League league = new League();
        league.setId(2001L);
        league.setName("Test League");
        league.setWeight(10);
        league.setSportName(sportName);

        Event event = new Event();
        event.setId(3001L);
        event.setName("Test Event");

        List<League> leagues = new ArrayList<>(List.of(league));
        Region region = new Region("Test Region", leagues);
        List<Region> regions = new ArrayList<>(List.of(region));

        Sport sport = new Sport(
                1L,
                sportName,
                regions
        );

        when(apiClient.fetchBaseInformation()).thenReturn(CompletableFuture.completedFuture(List.of(sport)));
        when(apiClient.fetchEventsForLeague(league)).thenReturn(CompletableFuture.completedFuture(List.of(event)));

        Event detailedEvent = new Event();
        detailedEvent.setId(3001L);
        detailedEvent.setName("Detailed Event");
        LeonParser spyParser = spy(parser);

        doReturn(CompletableFuture.completedFuture(detailedEvent)).when(spyParser).processEvent(event);

        // Act
        spyParser.processData();

        // Assert
        verify(displayService).displayLeagueInfo(league);
        verify(displayService).displayEvent(detailedEvent);
    }
}