package com.zemnitskiy.model;

public record League(long id, String name, int weight, boolean top, int topOrder, Sport sport) {
}