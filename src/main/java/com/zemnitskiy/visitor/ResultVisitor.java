package com.zemnitskiy.visitor;

import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.RootResult;
import com.zemnitskiy.model.result.RunnerResult;

public interface ResultVisitor {
    void visitRoot(RootResult rootResult);
    void visitLeague(LeagueResult leagueResult);
    void visitMatch(MatchResult matchResult);
    void visitMarket(MarketResult marketResult);
    void visitRunner(RunnerResult runnerResult);
}