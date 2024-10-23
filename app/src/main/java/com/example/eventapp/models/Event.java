package com.example.eventapp.models;

public class Event {
    private String eventName;

    public Event(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
