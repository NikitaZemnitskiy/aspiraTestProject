package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Market;
import com.zemnitskiy.visitor.Result;
import com.zemnitskiy.visitor.ResultVisitor;

import java.util.List;

public record MarketResult(Market market, List<RunnerResult> runnerResults) implements Result {

    @Override
    public void accept(ResultVisitor v) {
        v.visitMarket(this);
        for (RunnerResult runnerResult : runnerResults) {
            runnerResult.accept(v);
        }
    }
}

