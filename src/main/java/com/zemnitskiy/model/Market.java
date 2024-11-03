package com.zemnitskiy.model;

import java.util.List;

public record Market(long id, String name, List<Runner> runners) { }

