package com.example.eventapp.models;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Exclude;

public class Signup implements HasDocumentId {
    @Exclude
    private String documentId;
    private String userId;
    private String eventId;
    private long signupTimestamp;
    private boolean isCancelled;

    public Signup() {
        // default constructor for firebase
    }

    public Signup(String userId, String eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getSignupTimestamp() {
        return signupTimestamp;
    }

    public void setSignupTimestamp(long signupTimestamp) {
        this.signupTimestamp = signupTimestamp;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
