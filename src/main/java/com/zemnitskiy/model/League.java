package com.zemnitskiy.model;

public class League {
    private long id;
    private Sport sport;
    private String name;
    private String nameDefault;
    private String url;
    private int weight;
    private int prematch;
    private int inplay;
    private int outright;
    private boolean top;
    private boolean hasZeroMarginEvents;
    private int topOrder;
    private Region region;
    private String logoSource;
    private String logoUrl;
    private String background;
    private String sportName;

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameDefault() {
        return nameDefault;
    }

    public void setNameDefault(String nameDefault) {
        this.nameDefault = nameDefault;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPrematch() {
        return prematch;
    }

    public void setPrematch(int prematch) {
        this.prematch = prematch;
    }

    public int getInplay() {
        return inplay;
    }

    public void setInplay(int inplay) {
        this.inplay = inplay;
    }

    public int getOutright() {
        return outright;
    }

    public void setOutright(int outright) {
        this.outright = outright;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isHasZeroMarginEvents() {
        return hasZeroMarginEvents;
    }

    public void setHasZeroMarginEvents(boolean hasZeroMarginEvents) {
        this.hasZeroMarginEvents = hasZeroMarginEvents;
    }

    public int getTopOrder() {
        return topOrder;
    }

    public void setTopOrder(int topOrder) {
        this.topOrder = topOrder;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getLogoSource() {
        return logoSource;
    }

    public void setLogoSource(String logoSource) {
        this.logoSource = logoSource;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBackground() {
        return background;
    }

    @Override
    public String toString() {
        return "League{" +
                "name='" + name + '\'' +
                '}';
    }

    public void setBackground(String background) {
        this.background = background;
    }
}
