package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Runner;
import com.zemnitskiy.visitor.Result;
import com.zemnitskiy.visitor.ResultVisitor;

public record RunnerResult(Runner runner) implements Result {

    @Override
    public void accept(ResultVisitor v) {
        v.visitRunner(this);
    }
}
