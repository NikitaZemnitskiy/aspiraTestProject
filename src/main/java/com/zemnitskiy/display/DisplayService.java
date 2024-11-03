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

    private static final String INDENT_LEAGUE = "";
    private static final String INDENT_EVENT = "    ";
    private static final String INDENT_MARKET = "        ";
    private static final String INDENT_RUNNER = "            ";

    public void displaySportAndLeagueInfo(String sportName, League league) {
        System.out.println(INDENT_LEAGUE + "Sport - %s, %s".formatted(sportName, league.name()));
    }

    public void displayEvent(Event event) {
        if (event != null) {
            LocalDateTime kickoffTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getKickoff()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println(INDENT_EVENT + "%s %s, %d".formatted(
                    event.getName(),
                    kickoffTime.format(formatter),
                    event.getId()
            ));
            displayMarkets(event.getMarkets());
        } else {
            System.out.println(INDENT_EVENT + "Event details are not available.");
        }
    }

    public void displayMarkets(List<Market> markets) {
        if (markets != null && !markets.isEmpty()) {
            for (Market market : markets) {
                System.out.println(INDENT_MARKET + market.getName());
                for (Runner runner : market.getRunners()) {
                    System.out.println(INDENT_RUNNER + "%s, %s, %d".formatted(
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
