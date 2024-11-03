package com.zemnitskiy.model;

import java.util.List;

public record Event(long id, String name, long kickoff, List<Market> markets) {
}
