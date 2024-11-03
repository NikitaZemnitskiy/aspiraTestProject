package com.zemnitskiy.util;

import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Region;

import java.util.Comparator;
import java.util.Map;

public class ComparatorUtils {

    private static final Map<String, Integer> FOOTBALL_COUNTRY_PRIORITY = Map.ofEntries(
            Map.entry("Europe", 0),
            Map.entry("England", 1),
            Map.entry("France", 2),
            Map.entry("Germany", 3),
            Map.entry("Italy", 4),
            Map.entry("Spain", 5)
    );

    private static final Map<String, Integer> FOOTBALL_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("UEFA Champions League", 0),
            Map.entry("Premier League", 1),
            Map.entry("League 1", 2),
            Map.entry("Bundesliga", 3),
            Map.entry("Serie A", 4),
            Map.entry("LaLiga", 5)
    );

    private static final Map<String, Integer> ICE_HOCKEY_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("NHL", 0),
            Map.entry("KHL", 1),
            Map.entry("Liiga", 2),
            Map.entry("DEL", 3)
    );

    private static final Map<String, Integer> BASKETBALL_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("1970324836975913", 0),
            Map.entry("1970324836982310", 1),
            Map.entry("1970324836976051", 2),
            Map.entry("1970324836974725", 3)
    );

    private static final Map<String, Integer> TENNIS_LEAGUE_PRIORITY = Map.ofEntries(
            Map.entry("Paris", 0),
            Map.entry("Hong Kong", 1),
            Map.entry("Jiujiang", 2),
            Map.entry("Merida", 3)
    );

    public static Comparator<Region> getRegionComparator(String sportName) {
        return switch (sportName) {
            case "Football" ->
                    Comparator.comparingInt(region ->
                            FOOTBALL_COUNTRY_PRIORITY.getOrDefault(region.name(), Integer.MAX_VALUE)
                    );
            default -> Comparator.comparingInt(_ -> 0);
        };
    }

    public static Comparator<League> getLeagueComparator(String sportName) {
        return switch (sportName) {
            case "Football" ->
                    Comparator.comparingInt(league ->
                            FOOTBALL_LEAGUE_PRIORITY.getOrDefault(league.name(), Integer.MAX_VALUE)
                    );
            case "Tennis" ->
                    Comparator.comparingInt(league ->
                            TENNIS_LEAGUE_PRIORITY.getOrDefault(league.name(), Integer.MAX_VALUE)
                    );
            case "Ice Hockey" ->
                    Comparator.comparingInt(league ->
                            ICE_HOCKEY_LEAGUE_PRIORITY.getOrDefault(league.name(), Integer.MAX_VALUE)
                    );
            case "Basketball" ->
                    Comparator.comparingInt(league ->
                            BASKETBALL_LEAGUE_PRIORITY.getOrDefault(String.valueOf(league.id()), Integer.MAX_VALUE)
                    );
            default -> Comparator.comparingInt(_ -> 0);
        };
    }
}
