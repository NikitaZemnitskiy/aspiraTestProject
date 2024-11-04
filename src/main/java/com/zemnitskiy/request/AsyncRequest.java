package com.zemnitskiy.request;

import java.util.concurrent.CompletableFuture;

public sealed interface AsyncRequest<R> permits EventRequest, LeagueRequest, RootRequest {
    CompletableFuture<R> fetch();
}

