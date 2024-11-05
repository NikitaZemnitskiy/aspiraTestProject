package com.zemnitskiy.model.result;

import com.zemnitskiy.visitor.Result;
import com.zemnitskiy.visitor.ResultVisitor;

import java.util.List;

public record RootResult(List<LeagueResult> leagueResults) implements Result {

    @Override
    public void accept(ResultVisitor v) {
        v.visitRoot(this);
        for (LeagueResult leagueResult : leagueResults) {
            leagueResult.accept(v);
        }
    }
}
