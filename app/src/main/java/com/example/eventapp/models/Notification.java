package com.example.eventapp.models;

import com.google.firebase.firestore.Exclude;

public class Notification {
    @Exclude
    private String documentId;

    private String userId;
    private String title;
    private String message;
    private String event;
    private String type;
    private Boolean read;

    public Notification() {
    }

    // Constructor for invitation notification
    public Notification(String userId, String title, String message, String event, String type, boolean read) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.event = event;
        this.type = type;
        this.read = read;
    }

    // Constructor for general notification
    public Notification(String userId, String title, String message, String type, boolean read) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
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

    public String getEvent() { return event; }

    public void setEvent(String event) { this.event = event; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRead() { return read; }

    public void setRead(Boolean read) { this.read = read; }
}