package com.zemnitskiy.model.basemodel;

import java.util.List;

public record Market(long id, String name, List<Runner> runners) { }

