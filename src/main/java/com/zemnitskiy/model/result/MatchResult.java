package com.zemnitskiy.model.result;

import com.zemnitskiy.model.basemodel.Event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public record MatchResult(Event event, List<MarketResult> marketResults) implements ResultVisitor {

    @Override
    public void visit() {
        LocalDateTime kickoffTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.kickoff()), ZoneId.systemDefault());
        System.out.println("\t" + String.format("%s %s, %d",
                event.name(),
                kickoffTime,
                event.id()));
        marketResults.forEach(MarketResult::visit);
    }
}