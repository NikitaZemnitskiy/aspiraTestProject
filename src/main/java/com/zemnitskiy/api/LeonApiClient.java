package com.zemnitskiy.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zemnitskiy.model.basemodel.Event;
import com.zemnitskiy.model.basemodel.League;
import com.zemnitskiy.model.basemodel.Sport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.zemnitskiy.Main.LOCALE;
import static com.zemnitskiy.Main.PARAMETERS;

/**
 * The {@code LeonApiClient} class provides methods to interact with the Leonbets API.
 * It facilitates fetching base information about sports, retrieving events for a specific league,
 * and obtaining detailed information about a particular event.
 *
 * <p>This client utilizes {@link HttpClient} to perform asynchronous HTTP requests and
 * parses JSON responses using Gson.</p>
 *
 * @see HttpClient
 * @see CompletableFuture
 * @see Gson
 *
 */
public class LeonApiClient {
    /**
     * The {@link HttpClient} instance used to perform HTTP requests.
     */
    private final HttpClient httpClient;
    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(LeonApiClient.class);

    /**
     * The base URL for the Leonbets API.
     */
    private final String baseUrl;

    /**
     * Constructs a new {@code LeonApiClient} with the specified {@link HttpClient} and base URL.
     *
     * @param httpClient the {@link HttpClient} to be used for HTTP requests
     * @throws IllegalArgumentException if {@code baseUrl} is null or empty
     */
    public LeonApiClient(HttpClient httpClient, ExecutorService executorService, String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL must not be null or empty.");
        }
        this.httpClient = httpClient;
        this.executorService = executorService;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetches the base information about available sports from the Leonbets API.
     *
     * <p>This method sends an asynchronous GET request to the endpoint
     * {@code {baseUrl}/betline/sports} with query parameters {@code ctag} and {@code flags}.
     * The response is expected to be a JSON array of {@link Sport} objects.</p>
     *
     * @return a {@link CompletableFuture} that, when completed, returns a {@link List} of {@link Sport} objects
     * @throws IllegalArgumentException if the HTTP response status code is not {@code 200 OK}
     */
    public CompletableFuture<List<Sport>> fetchBaseInformation() {
        logger.debug("Fetching base information from ");
        String url = baseUrl + "betline/sports?ctag=" + LOCALE + "&flags=urlv2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    checkResponse(response);
                    Type sportListType = new TypeToken<List<Sport>>() {}.getType();
                    return new Gson().fromJson(response.body(), sportListType);
                }, executorService);
    }

    /**
     * Fetches events for a specific league from the Leonbets API.
     *
     * <p>This method sends an asynchronous GET request to the endpoint
     * {@code {baseUrl}/betline/events/all} with query parameters {@code ctag}, {@code league_id},
     * {@code hideClosed}, and {@code flags}. The response is expected to be a JSON representation
     * of a {@link League} object, potentially updated with event information.</p>
     *
     * @param league the {@link League} for which to fetch events
     * @return a {@link CompletableFuture} that, when completed, returns an updated {@link League} object
     * @throws IllegalArgumentException if the HTTP response status code is not {@code 200 OK}
     */
    public CompletableFuture<League> fetchEventsForLeague(League league) {
        logger.debug("Fetching events for league {}", league);
        String url = String.format(baseUrl + "betline/events/all?ctag=%s&league_id=%d&hideClosed=%b&flags=%s",
                LOCALE, league.id(), true, PARAMETERS);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    checkResponse(response);
                    return new Gson().fromJson(response.body(), League.class);
                }, executorService);
    }

    /**
     * Fetches detailed information about a specific event from the Leonbets API.
     *
     * <p>This method sends an asynchronous GET request to the endpoint
     * {@code {baseUrl}/betline/event/all} with query parameters {@code eventId}, {@code ctag},
     * {@code hideClosed}, and {@code flags}. The response is expected to be a JSON representation
     * of an {@link Event} object.</p>
     *
     * @param eventId the unique identifier of the {@link Event} to fetch details for
     * @return a {@link CompletableFuture} that, when completed, returns an {@link Event} object with detailed information
     * @throws IllegalArgumentException if the HTTP response status code is not {@code 200 OK}
     */
    public CompletableFuture<Event> fetchEventDetails(long eventId) {
        logger.debug("Fetching event details for event {}", eventId);
        String url = String.format(baseUrl + "betline/event/all?eventId=%d&ctag=%s&hideClosed=%b&flags=%s",
                eventId, LOCALE, true, PARAMETERS);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    checkResponse(response);
                    return new Gson().fromJson(response.body(), Event.class);
                }, executorService);
    }

    private void checkResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("Failed to fetch data. Response code: " + response.statusCode());
        }
    }
}
