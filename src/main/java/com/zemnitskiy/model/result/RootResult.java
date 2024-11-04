package com.zemnitskiy.model.result;

import java.util.List;

public record RootResult(List<LeagueResult> leagueResults) implements ResultVisitor {
    @Override
    public void visit() {
        leagueResults.forEach(LeagueResult::visit);
    }
}
