package com.zemnitskiy.display;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Market;
import com.zemnitskiy.model.Runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DisplayService {

    private static final Logger logger = LoggerFactory.getLogger(DisplayService.class);

    private static final String INDENT_LEAGUE = "";
    private static final String INDENT_EVENT = "    ";
    private static final String INDENT_MARKET = "        ";
    private static final String INDENT_RUNNER = "            ";

    public void displaySportAndLeagueInfo(String sportName, League league) {
        logger.info(INDENT_LEAGUE + "Sport - {}, {}", sportName, league.name());
    }

    public void displayEvent(Event event) {
        if (event != null) {
            LocalDateTime kickoffTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getKickoff()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            logger.info(INDENT_EVENT + "{} {}, {}",
                    event.getName(),
                    kickoffTime.format(formatter),
                    event.getId());
            displayMarkets(event.getMarkets());
        } else {
            logger.info(INDENT_EVENT + "Event details are not available.");
        }
    }

    public void displayMarkets(List<Market> markets) {
        if (markets != null && !markets.isEmpty()) {
            for (Market market : markets) {
                logger.info(INDENT_MARKET + "{}", market.getName());
                for (Runner runner : market.getRunners()) {
                    logger.info(INDENT_RUNNER + "{}, {}, {}",
                            runner.name(),
                            runner.priceStr(),
                            runner.id());
                }
            }
        } else {
            logger.info(INDENT_MARKET + "No markets available.");
        }
    }
}