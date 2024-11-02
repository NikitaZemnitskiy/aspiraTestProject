package com.zemnitskiy.model;

import java.util.List;

public class SportsResponse {
    private boolean enabled;
    private Object betline;
    private int totalCount;
    private String vtag;
    private List<Event> events;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Object getBetline() {
        return betline;
    }

    public void setBetline(Object betline) {
        this.betline = betline;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getVtag() {
        return vtag;
    }

    public void setVtag(String vtag) {
        this.vtag = vtag;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
