package com.zemnitskiy.model.basemodel;

import java.util.List;

public record Sport(long id, String name, List<Region> regions) { }
