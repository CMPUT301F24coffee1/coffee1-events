package com.example.eventapp.models;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Represents a custom notification that can be sent to users.
 */
public class Notification implements Serializable, HasDocumentId {
    @Exclude
    private String documentId;

    private String userId;
    private String title;
    private String message;
    private String eventId;
    private String type;

    public Notification() {
    }

    /**
     * Constructor for an invitation notification
     * @param userId the userId of the user receiving the notification
     * @param title the title of the notification
     * @param message the message displayed in the notification
     * @param eventId the eventId of the event that the user is invited to enroll in
     */
    public Notification(String userId, String title, String message, String eventId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.eventId = eventId;
        this.type = "Invite";
    }

    /**
     * Constructor for a general notification
     * @param userId the userId of the user receiving the notification
     * @param title the title of the notification
     * @param message the message displayed in the notification
     */
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
