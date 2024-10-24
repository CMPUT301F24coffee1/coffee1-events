package com.example.eventapp.models;

public class Event {
    private String eventName;
    private String eventDescription;
    private boolean geolocationRequired;
    private int maxEntrants; //-1 for no max

    public Event(String name) {
        this.eventName = name;
        this.eventDescription = "";
    }

    public Event(String name, String description) {
        this.eventName = name;
        this.eventDescription = description;
        this.maxEntrants = -1;
    }

    public Event(String name, String description, boolean geolocationRequired) {
        this.eventName = name;
        this.eventDescription = description;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = -1;
    }

    public Event(String name, String description, boolean geolocationRequired, int maxEntrants) {
        this.eventName = name;
        this.eventDescription = description;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = maxEntrants;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public int getMaxEntrants() {
        return maxEntrants;
    }

    public void setMaxEntrants(int maxEntrants) {
        this.maxEntrants = maxEntrants;
    }
}