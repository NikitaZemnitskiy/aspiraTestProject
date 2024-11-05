package com.zemnitskiy.request;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an asynchronous request that can fetch data of type {@code R}.
 *
 * <p>This interface is sealed and permits only specific implementations:
 * {@link EventRequest}, {@link LeagueRequest}, and {@link RootRequest}.</p>
 *
 * @param <R> the type of result produced by the asynchronous request
 * @see CompletableFuture
 * @see EventRequest
 * @see LeagueRequest
 * @see RootRequest
 */
public sealed interface AsyncRequest<R> permits EventRequest, LeagueRequest, RootRequest {

    /**
     * Initiates the asynchronous fetch operation.
     *
     * @return a {@link CompletableFuture} that, when completed, yields a result of type {@code R}
     * @throws RuntimeException if the fetch operation fails
     */
    CompletableFuture<R> fetch();
}
