package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Runner;

public record RunnerResult(Runner runner) implements ResultVisitor {

    @Override
    public void visit() {
            System.out.println("\t\t\t" + String.format("%s, %s, %d",
                    runner.name(),
                    runner.priceStr(),
                    runner.id()
            ));
    }
}
