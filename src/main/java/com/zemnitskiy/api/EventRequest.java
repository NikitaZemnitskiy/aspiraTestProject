package com.zemnitskiy.api;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.result.EventResult;

import java.util.concurrent.CompletableFuture;

public class EventRequest implements AsyncRequest<EventResult> {
    private final LeonApiClient apiClient;
    private final Event event;

    public EventRequest(LeonApiClient apiClient, Event event) {
        this.apiClient = apiClient;
        this.event = event;
    }

    @Override
    public CompletableFuture<EventResult> fetch() {
        return apiClient.fetchEventDetails(event.id())
                .thenApply(EventResult::new);
    }
}
