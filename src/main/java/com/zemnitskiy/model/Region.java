package com.zemnitskiy.model;

import java.util.List;

public record Region(String name, List<League> leagues) { }
