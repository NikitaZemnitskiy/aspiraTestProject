package com.zemnitskiy.model;

public class Competitor {
    private long id;
    private String name;
    private String homeAway;
    private String logoSource;
    private String logo;

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

    public String getHomeAway() {
        return homeAway;
    }

    public void setHomeAway(String homeAway) {
        this.homeAway = homeAway;
    }

    public String getLogoSource() {
        return logoSource;
    }

    public void setLogoSource(String logoSource) {
        this.logoSource = logoSource;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}