package com.zemnitskiy.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.SportsResponse;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeonApiClient {

    private static final String BASE_URL = "https://leonbets.com/api-2/";
    private static final String LOCALE = "en-US";
    private static final String PARAMETERS = "reg,urlv2,mm2,rrc,nodup";
    private final ExecutorService executorService;

    private final HttpClient httpClient;

    public LeonApiClient() {
        this.executorService = Executors.newFixedThreadPool(3);
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build();
    }

    public CompletableFuture<List<Sport>> fetchBaseInformation() {
        String url = BASE_URL + "betline/sports?ctag=" + LOCALE + "&flags=urlv2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Type sportListType = new TypeToken<List<Sport>>() {
                        }.getType();
                        return new Gson().fromJson(response.body(), sportListType);
                    } else {
                        throw new RuntimeException("Failed to fetch sports data. Response code: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<List<Event>> fetchEventsForLeague(League league) {
        String url = String.format(BASE_URL + "betline/events/all?ctag=%s&league_id=%d&hideClosed=%b&flags=%s",
                LOCALE, league.id(), true, PARAMETERS);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        SportsResponse sportsResponse = new Gson().fromJson(response.body(), SportsResponse.class);
                        return sportsResponse.events().stream().limit(2).toList();
                    } else {
                        throw new RuntimeException("Failed to fetch events for league " + league.name() + ". Response code: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Event> fetchEventDetails(long eventId) {
        String url = String.format(BASE_URL + "betline/event/all?eventId=%d&ctag=%s&hideClosed=%b&flags=%s",
                eventId, LOCALE, true, PARAMETERS);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return new Gson().fromJson(response.body(), Event.class);
                    } else {
                        throw new RuntimeException("Failed to fetch details for event " + eventId + ". Response code: " + response.statusCode());
                    }
                });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

