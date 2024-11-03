package com.zemnitskiy.api;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.League;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.SportsResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeonApiClient {

    private static final String LOCALE = "en-US";
    private static final String PARAMETERS = "reg,urlv2,mm2,rrc,nodup";

    private final LeonApiService apiService;

    public LeonApiClient(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.apiService = retrofit.create(LeonApiService.class);
    }

    public List<Sport> fetchBaseInformation() throws IOException {
        Call<List<Sport>> call = apiService.getBaseInformation(LOCALE, "urlv2");
        Response<List<Sport>> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            System.err.println("Failed to fetch sports data. Response code: " + response.code());
            return Collections.emptyList();
        }
    }

    public List<Event> fetchEventsForLeague(League league) throws IOException {
        Call<SportsResponse> call = apiService.getSportResponse(
                LOCALE,
                league.getId(),
                true,
                PARAMETERS
        );
        Response<SportsResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body().events().stream().limit(2).toList();
        } else {
            System.err.printf(
                    "Failed to fetch events for leagues %s. Response code: %d%n", league.getName(),
                    response.code()
            );
            return Collections.emptyList();
        }
    }

    public Event fetchEventDetails(long eventId) throws IOException {
        Call<Event> call = apiService.getEvent(
                eventId,
                LOCALE,
                true,
                PARAMETERS
        );
        Response<Event> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            System.err.printf(
                    "Failed to fetch details for event %d. Response code: %d%n", eventId,
                    response.code()
            );
            return null;
        }
    }
}

