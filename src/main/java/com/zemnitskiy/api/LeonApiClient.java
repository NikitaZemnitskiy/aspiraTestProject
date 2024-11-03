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

import static com.zemnitskiy.parser.LeonParser.BASE_URL;
import static com.zemnitskiy.parser.LeonParser.LOCALE;
import static com.zemnitskiy.parser.LeonParser.PARAMETERS;

public class LeonApiClient {
    private final HttpClient httpClient;

    public LeonApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<List<Sport>> fetchBaseInformation() {
        String url = BASE_URL + "betline/sports?ctag=" + LOCALE + "&flags=urlv2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    checkResponse(response);
                    Type sportListType = new TypeToken<List<Sport>>() {
                    }.getType();
                    return new Gson().fromJson(response.body(), sportListType);
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
                        checkResponse(response);
                        SportsResponse sportsResponse = new Gson().fromJson(response.body(), SportsResponse.class);
                        return sportsResponse.events().stream().limit(2).toList();
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
                .thenApply(response -> new Gson().fromJson(response.body(), Event.class));
    }

    private void checkResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("Failed to fetch sports data. Response code: " + response.statusCode());
        }

    }
}

