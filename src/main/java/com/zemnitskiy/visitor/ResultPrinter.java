package com.zemnitskiy.visitor;

import com.zemnitskiy.model.result.LeagueResult;
import com.zemnitskiy.model.result.MarketResult;
import com.zemnitskiy.model.result.MatchResult;
import com.zemnitskiy.model.result.RootResult;
import com.zemnitskiy.model.result.RunnerResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ResultPrinter implements ResultVisitor {

    @Override
    public void visitRoot(RootResult rootResult) {
        // Not needed to display
    }

    @Override
    public void visitLeague(LeagueResult leagueResult) {
        System.out.printf("%s, %s%n", leagueResult.sportName(), leagueResult.league().name());
    }

    @Override
    public void visitMatch(MatchResult matchResult) {
        LocalDateTime kickoffTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(matchResult.event().kickoff()), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("\t" + String.format("%s %s, %d",
                matchResult.event().name(),
                kickoffTime.format(formatter) + " UTC",
                matchResult.event().id()));
    }

    @Override
    public void visitMarket(MarketResult marketResult) {
        System.out.println("\t\t" + marketResult.market().name());
    }

    @Override
    public void visitRunner(RunnerResult runnerResult) {
        System.out.println("\t\t\t" + String.format("%s, %s, %d",
                runnerResult.runner().name(),
                runnerResult.runner().priceStr(),
                runnerResult.runner().id()
        ));
    }
}
