package com.zemnitskiy.model;

import java.util.List;

public record Sport(long id, String name, List<Region> regions) {}
