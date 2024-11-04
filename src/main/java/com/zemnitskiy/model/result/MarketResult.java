package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Market;

import java.util.List;

public record MarketResult(Market market, List<RunnerResult> runnerResults) implements ResultVisitor {

    @Override
    public void visit() {
        System.out.println("\t\t" + market.name());
        runnerResults.forEach(RunnerResult::visit);
    }
}

