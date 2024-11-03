package com.zemnitskiy.model;

import java.util.List;

public class LeagueResult {
    private final League league;
    private final List<Event> events;

    public LeagueResult(League league, List<Event> events) {
        this.league = league;
        this.events = events;
    }

    public League getLeague() {
        return league;
    }

    public List<Event> getEvents() {
        return events;
    }
}
