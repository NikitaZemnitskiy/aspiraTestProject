package com.zemnitskiy.model.basemodel;

import java.util.List;

public record League(long id, String name, int weight, boolean top, int topOrder, List<Event> events) { }