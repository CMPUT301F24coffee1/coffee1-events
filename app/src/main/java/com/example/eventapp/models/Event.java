package com.example.eventapp.models;

public class Event {
    private String eventName;
    private String eventDescription;

    public Event(String name) {
        this.eventName = name;
        this.eventDescription = "";
    }

    public Event(String name, String description) {
        this.eventName = name;
        this.eventDescription = description;
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
}