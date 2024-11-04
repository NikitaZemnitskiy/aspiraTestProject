package com.zemnitskiy.model.result;

import com.zemnitskiy.model.Sport;

import java.util.List;

public record RootResult(Sport sport, List<LeagueResult> leagueResults) { }
