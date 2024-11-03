package com.zemnitskiy.model.result;

import java.util.List;

public record SportResult(String sportName, List<LeagueResult> leagueResults) {
}
