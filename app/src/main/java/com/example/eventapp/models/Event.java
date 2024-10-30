package com.example.eventapp.models;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.Exclude;

import java.net.URI;

public class Event {
    @Exclude
    private String documentId;
    private String organizerId;

    private String eventName;
    private Uri posterUri;
    private String eventDescription;
    private boolean geolocationRequired;
    private int maxEntrants; //-1 for no max
    private long startDate;
    private long endDate;
    private long deadline;
    private Bitmap qrCode;

    public Event() {
        // default constructor for firebase
    }

    // for testing
    public Event(String name, String description) {
        this.eventName = name;
        this.eventDescription = description;
        this.maxEntrants = -1;
    }

    public Event(String eventName, Uri posterUri, String eventDescription, boolean geolocationRequired, long startDate, long endDate, long deadline, Bitmap qrCode) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = -1;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
        this.qrCode = qrCode;
    }

    // Constructor with poster attribute
    public Event(String eventName, Uri posterUri, String eventDescription, boolean geolocationRequired, int maxEntrants, long startDate, long endDate, long deadline, Bitmap qrCode) {
        this.eventName = eventName;
        this.posterUri = posterUri;
        this.eventDescription = eventDescription;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = maxEntrants;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
        this.qrCode = qrCode;
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

    public Uri getPosterUri() {
        return posterUri;
    }

    public void setPosterUri(Uri posterUri) {
        this.posterUri = posterUri;
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