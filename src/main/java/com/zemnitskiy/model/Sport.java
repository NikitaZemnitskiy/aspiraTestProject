package com.zemnitskiy.model;

import java.util.List;

public class Sport {
    private long id;
    private String name;
    private int weight;
    private String family;
    private List<Region> regions;
    private List<MainMarket> mainMarkets;
    private boolean isVirtual;
    private String url;

    // Getters and setters

    public long getId() {
        return id;
    }

    public List<MainMarket> getMainMarkets() {
        return mainMarkets;
    }

    public void setMainMarkets(List<MainMarket> mainMarkets) {
        this.mainMarkets = mainMarkets;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    @Override
    public String toString() {
        return "Sport{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", family='" + family + '\'' +
                ", regions=" + regions +
                '}';
    }
}
