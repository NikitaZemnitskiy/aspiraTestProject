package com.zemnitskiy.model;

import java.util.List;

public record SportsResponse(
        boolean enabled,
        Object betline,
        int totalCount,
        String vtag,
        List<Event> events) {}
