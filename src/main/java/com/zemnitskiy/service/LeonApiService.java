package com.zemnitskiy.service;

import com.zemnitskiy.model.Event;
import com.zemnitskiy.model.Sport;
import com.zemnitskiy.model.SportsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface LeonApiService {

    @GET("betline/sports")
    Call<List<Sport>> getBaseInformation(
            @Query("ctag") String language,
            @Query("flags") String flags
    );

    @GET("betline/events/all")
    Call<SportsResponse> getSportResponse(
            @Query("ctag") String language,
            @Query("league_id") long leagueId,
            @Query("hideClosed") boolean hideClosed,
            @Query("flags") String flags
    );

    @GET("betline/event/all")
    Call<Event> getEvent(
            @Query("eventId") long eventId,
            @Query("ctag") String language,
            @Query("hideClosed") boolean hideClosed,
            @Query("flags") String flags
    );
}
