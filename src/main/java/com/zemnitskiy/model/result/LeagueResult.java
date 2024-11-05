package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.visitor.Result;
import com.zemnitskiy.visitor.ResultVisitor;

import java.util.List;

public record LeagueResult(String sportName, League league, List<MatchResult> matchResults) implements Result {

    @Override
    public void accept(ResultVisitor v) {
        v.visitLeague(this);
        for (MatchResult matchResult : matchResults) {
            matchResult.accept(v);
        }
    }
}
