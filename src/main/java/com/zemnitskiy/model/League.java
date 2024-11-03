package com.zemnitskiy.model;

public record League(long id, String name, int weight, String sportName) {
    public League(long id, String name, int weight, String sportName) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.sportName = sportName;
    }
}