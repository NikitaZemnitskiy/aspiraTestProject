package com.zemnitskiy.model.result;

import com.zemnitskiy.model.League;

import java.util.List;

public record LeagueResult(League league, List<EventResult> eventResults) {
}