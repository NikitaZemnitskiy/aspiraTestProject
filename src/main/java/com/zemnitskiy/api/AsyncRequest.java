package com.zemnitskiy.api;

import java.util.concurrent.CompletableFuture;

public interface AsyncRequest<R> {
    CompletableFuture<R> fetch();
}

