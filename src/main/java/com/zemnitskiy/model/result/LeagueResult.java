package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.League;

import java.util.List;

public record LeagueResult(String sportName, League league, List<MatchResult> matchResults) implements ResultVisitor {

    @Override
    public void visit() {
        System.out.printf("%s, %s%n", sportName, league.name());
        matchResults.forEach(MatchResult::visit);
    }
}
