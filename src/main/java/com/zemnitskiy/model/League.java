package com.zemnitskiy.model;

public class League {
    private long id;
    private String name;
    private int weight;
    private String sportName;

    public League(long id, String name, int weight, String sportName) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.sportName = sportName;
    }

    public League() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }
}