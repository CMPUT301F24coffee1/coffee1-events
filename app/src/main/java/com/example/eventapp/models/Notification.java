package com.example.eventapp.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Notification implements Serializable {
    @Exclude
    private String documentId;

    private String userId;
    private String title;
    private String message;
    private String eventId;
    private String type;

    public Notification() {
    }

    // Constructor for invitation notification
    public Notification(String userId, String title, String message, String eventId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.eventId = eventId;
        this.type = "Invite";
    }

    // Constructor for general notification
    public Notification(String userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = "General";
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String id) {
        this.documentId = id;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() { return eventId; }

    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
