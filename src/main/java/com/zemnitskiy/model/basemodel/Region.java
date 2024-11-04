package com.zemnitskiy.model.basemodel;

import java.util.List;

public record Region(String name, List<League> leagues) { }
