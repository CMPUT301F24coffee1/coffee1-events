package com.example.eventapp.models;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Exclude;

/**
 * Represents a Signup record for an Event, containing information about the user and event,
 * as well as the signup timestamp and cancellation status.
 * Implements the {@link HasDocumentId} interface for Firestore integration.
 */
public class Signup implements HasDocumentId {
    @Exclude
    private String documentId;
    private String userId;
    private Double latitude;
    private Double longitude;
    private String eventId;
    private long signupTimestamp;

    private boolean isCancelled = false;
    private boolean isWaitlisted = false;
    // Chosen means that a user is selected, but has not accepted the invitation yet
    private boolean isChosen = false;
    private boolean isEnrolled = false;

    public Signup() {
        // default constructor for firebase
    }


    /**
     * Creates a Signup with a specified user and event.
     *
     * @param userId  the ID of the user signing up
     * @param eventId the ID of the event to which the user is signing up
     */
    public Signup(String userId, String eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    /**
     * Creates a Signup with a specified user and event.
     *
     * @param userId  the ID of the user signing up
     * @param eventId the ID of the event to which the user is signing up
     */
    public Signup(String userId, String eventId, Double latitude, Double longitude) {
        this.userId = userId;
        this.eventId = eventId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the Firestore document ID for the signup.
     *
     * @return the document ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Gets the Firestore document ID for the signup.
     *
     * @param documentId the new document ID of the signup
     */
    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the user device ID associated with this signup.
     *
     * @return the user device ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user device ID associated with this signup.
     *
     * @param userId the new user device ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the latitude of the user of where they signed up
     *
     * @return the latitude of the user
     */
    public Double getLatitude() { return latitude; }

    /**
     * Sets the latitude of the user of where they signed up
     *
     * @param latitude the users current latitude
     */
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    /**
     * Gets the longitude of the user of where they signed up
     *
     * @return the longitude of the user
     */
    public Double getLongitude() { return longitude; }

    /**
     * Sets the longitude of the user of where they signed up
     *
     * @param longitude the users current longitude
     */
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    /**
     * Gets the event ID associated with this signup.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event ID associated with this signup.
     *
     * @param eventId the new event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the timestamp when the signup occurred.
     *
     * @return the signup timestamp in milliseconds
     */
    public long getSignupTimestamp() {
        return signupTimestamp;
    }

    /**
     * Sets the timestamp when the signup occurred.
     *
     * @param signupTimestamp - the signup timestamp in milliseconds
     */
    public void setSignupTimestamp(long signupTimestamp) {
        this.signupTimestamp = signupTimestamp;
    }

    /**
     * Checks if the signup has been cancelled.
     *
     * @return true if cancelled, otherwise false
     */
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets whether the signup has been cancelled.
     *
     * @param cancelled true if cancelled, otherwise false
     */
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    /**
     * Checks if the signed up user has been waitlisted.
     *
     * @return true if waitlisted, otherwise false
     */
    public boolean isWaitlisted() {
        return isWaitlisted;
    }

    /**
     * Sets if the signed up user has been waitlisted.
     *
     * @param waitlisted true if waitlisted, otherwise false
     */
    public void setWaitlisted(boolean waitlisted) {
        isWaitlisted = waitlisted;
    }

    /**
     * Checks if the signed up user has been chosen for the event.
     *
     * @return true if chosen, otherwise false
     */
    public boolean isChosen() {
        return isChosen;
    }

    /**
     * Sets if the signed up user has been chosen for the event.
     *
     * @param chosen true if chosen, otherwise false
     */
    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    /**
     * Checks if the signed up user has been enrolled into the event.
     *
     * @return true if enrolled, otherwise false
     */
    public boolean isEnrolled() {
        return isEnrolled;
    }

    /**
     * Sets if the signed up user has been enrolled into the event.
     *
     * @param enrolled true if enrolled, otherwise false
     */
    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }
}
