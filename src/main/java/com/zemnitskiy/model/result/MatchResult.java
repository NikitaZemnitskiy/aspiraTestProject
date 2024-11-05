package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.visitor.Result;
import com.zemnitskiy.visitor.ResultVisitor;

import java.util.List;

public record MatchResult(Event event, List<MarketResult> marketResults) implements Result {

    @Override
    public void accept(ResultVisitor v) {
        v.visitMatch(this);
        for (MarketResult marketResult : marketResults) {
            marketResult.accept(v);
        }
    }
}