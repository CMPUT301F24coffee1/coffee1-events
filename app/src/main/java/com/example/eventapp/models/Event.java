package com.example.eventapp.models;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

public class Event {
    @Exclude
    private String documentId;
    private String organizerId;
    private String facilityId;

    private String eventName;
    private String posterUriString;
    private String eventDescription;
    private boolean geolocationRequired;
    private int maxEntrants; //-1 for no max
    private long startDate;
    private long endDate;
    private long deadline;
    private String qrCodeHash;

    public Event() {
        // default constructor for firebase
    }

    // for testing
    public Event(String name, String description) {
        this.eventName = name;
        this.eventDescription = description;
        this.maxEntrants = -1;
    }

    public Event(String eventName, String posterUriString, String eventDescription, boolean geolocationRequired, long startDate, long endDate, long deadline) {
        this.eventName = eventName;
        this.posterUriString = posterUriString;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = -1;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
    }

    // Constructor with poster attribute
    public Event(String eventName, String posterUriString, String eventDescription, boolean geolocationRequired, int maxEntrants, long startDate, long endDate, long deadline) {
        this.eventName = eventName;
        this.posterUriString = posterUriString;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = maxEntrants;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOrganizerId() {
       return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
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

    public String getPosterUriString() {
        return posterUriString;
    }

    public void setPosterUriString(String posterUriString) {this.posterUriString = posterUriString;}

    //public Uri getPosterUri() { return posterUriString}

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

    public String getQrCodeHash() {
        return qrCodeHash;
    }

    public void setQrCodeHash(String qrCodeHash) {
        this.qrCodeHash = qrCodeHash;
    }
}