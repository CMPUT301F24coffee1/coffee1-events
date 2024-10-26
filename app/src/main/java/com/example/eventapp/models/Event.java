package com.example.eventapp.models;

import android.graphics.Bitmap;

public class Event {
    private String eventName;
    private String eventDescription;
    private boolean geolocationRequired;
    private int maxEntrants; //-1 for no max
    private long startDate;
    private long endDate;
    private long deadline;
    private Bitmap qrCode;

    // for testing
    public Event(String name, String description) {
        this.eventName = name;
        this.eventDescription = description;
        this.maxEntrants = -1;
    }

    public Event(String eventName, String eventDescription, boolean geolocationRequired, long startDate, long endDate, long deadline, Bitmap qrCode) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = -1;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
        this.qrCode = qrCode;
    }

    public Event(String eventName, String eventDescription, boolean geolocationRequired, int maxEntrants, long startDate, long endDate, long deadline, Bitmap qrCode) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = maxEntrants;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
        this.qrCode = qrCode;
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

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public Bitmap getQrCode() {
        return qrCode;
    }

    public void setQrCode(Bitmap qrCode) {
        this.qrCode = qrCode;
    }
}