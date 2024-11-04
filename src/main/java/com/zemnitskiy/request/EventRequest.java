package com.zemnitskiy.request;

import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.result.EventResult;

import java.util.concurrent.CompletableFuture;

public record EventRequest(LeonApiClient apiClient, Event event) implements AsyncRequest<EventResult> {

    @Override
    public CompletableFuture<EventResult> fetch() {
        return apiClient.fetchEventDetails(event.id())
                .thenApply(EventResult::new);
    }
}