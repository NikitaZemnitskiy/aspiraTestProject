package com.zemnitskiy.display;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Market;
import com.zemnitskiy.model.Runner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DisplayService {
    private static final String TAB = "\t";
    private static final String INDENT_EVENT = TAB;
    private static final String INDENT_MARKET = TAB + TAB;
    private static final String INDENT_RUNNER = TAB + TAB + TAB;

    public void displaySportAndLeagueInfo(String sportName, League league) {
        System.out.printf("%s, %s%n", sportName, league.name());
    }

    public void displayEvent(Event event) {
        if (event != null) {
            LocalDateTime kickoffTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.kickoff()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(INDENT_EVENT + String.format("%s %s, %d",
                    event.name(),
                    kickoffTime.format(formatter),
                    event.id()
            ));
            displayMarkets(event.markets());
        } else {
            System.out.println(INDENT_EVENT + "Event details are not available.");
        }
    }

    public void displayMarkets(List<Market> markets) {
        if (markets != null && !markets.isEmpty()) {
            for (Market market : markets) {
                System.out.println(INDENT_MARKET + market.name());
                for (Runner runner : market.runners()) {
                    System.out.println(INDENT_RUNNER + String.format("%s, %s, %d",
                            runner.name(),
                            runner.priceStr(),
                            runner.id()
                    ));
                }
            }
        } else {
            System.out.println(INDENT_MARKET + "No markets available.");
        }
    }
}