package com.example.eventapp.models;

import android.net.Uri;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Represents an Event in the application, containing details about the organizer,
 * associated facility, event description, poster, schedule, and registration limits.
 * Implements the {@link HasDocumentId} interface for Firestore integration.
 */
public class Event implements HasDocumentId, Serializable {
    @Exclude
    private String documentId;
    private String organizerId;
    private String facilityId;

    private int numberOfAttendees;
    private String eventName;
    private String posterUriString;
    private String eventDescription;
    private String qrCodeHash;
    private boolean geolocationRequired;
    private int maxEntrants; //-1 for no max
    private long startDate;
    private long endDate;
    private long deadline;

    public Event() {
        // default constructor for firebase
    }

    /**
     * Constructor for creating an Event with basic details.
     *
     * @param name        the name of the event
     * @param description a description of the event
     */
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
    public Event(String eventName,
                 String posterUriString,
                 String eventDescription,
                 int numberOfAttendees,
                 boolean geolocationRequired,
                 int maxEntrants,
                 long startDate,
                 long endDate,
                 long deadline) {
        this.eventName = eventName;
        this.posterUriString = posterUriString;
        this.eventDescription = eventDescription;
        this.numberOfAttendees = numberOfAttendees;
        this.geolocationRequired = geolocationRequired;
        this.maxEntrants = maxEntrants;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deadline = deadline;
    }

    /**
     * Returns the Firestore document ID for the event.
     *
     * @return the document ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the Firestore document ID for the event.
     *
     * @param documentId - the new document ID
     */
    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Returns the device ID of the event organizer.
     *
     * @return the organizer ID
     */
    public String getOrganizerId() {
       return organizerId;
    }

    /**
     * Sets the device ID of the event organizer.
     *
     * @param organizerId - the new organizer ID
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Gets the ID of the facility where the event is held.
     *
     * @return the facility ID
     */
    public String getFacilityId() {
        return facilityId;
    }

    /**
     * Sets the ID of the facility where the event is held.
     *
     * @param facilityId - the new facility ID
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    /**
     * Gets the number of attendees (which will be sampled by the lottery) for the event.
     *
     * @return number of event attendees.
     */
    public int getNumberOfAttendees() {
        return numberOfAttendees;
    }

    /**
     * Sets the number of attendees (which will be sampled by the lottery) for the event.
     *
     * @param numberOfAttendees number of attendees for the event (>= 1)
     */
    public void setNumberOfAttendees(int numberOfAttendees) {
        if (numberOfAttendees >= 1) {
            this.numberOfAttendees = numberOfAttendees;
        } else {
            throw new IllegalArgumentException("Number of event attendees must be >= 1");
        }
    }

    /**
     * Gets the name of the event.
     *
     * @return the name of the event
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the name of the event.
     *
     * @param eventName the name to set for the event
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the description of the event.
     *
     * @return the event description
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Sets the description for the event.
     *
     * @param eventDescription the description to set
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * Gets the URI string of the event's poster.
     *
     * @return the URI string of the event's poster, or null if not set
     */
    public String getPosterUriString() {
        return posterUriString;
    }

    /**
     * Gets the QR code hash associated with the event.
     *
     * @return the QR code hash for the event
     */
    public String getQrCodeHash() {
        return qrCodeHash;
    }

    /**
     * Sets the QR code hash for the event.
     *
     * @param newHash the new QR code hash to set
     */
    public void setQrCodeHash(String newHash) {
        this.qrCodeHash = newHash;
    }

    /**
     * Sets the URI string of the event's poster.
     *
     * @param posterUriString the URI string to set
     */
    public void setPosterUriString(String posterUriString) {this.posterUriString = posterUriString;}

    /**
     * Returns the URI of the event's poster.
     *
     * @return a URI pointing to the event's poster, or null if none exists
     */
    public Uri getPosterUri() {
        if (posterUriString != null && !posterUriString.isEmpty()) {
            return Uri.parse(posterUriString);
        } else {
            return null;
        }
    }

    /**
     * Determines if the event has a poster associated with it.
     *
     * @return true if a poster exists, otherwise false
     */
    public Boolean hasPoster() { return (posterUriString != null && !posterUriString.isEmpty()); }

    /**
     * Checks if geolocation is required for this event.
     *
     * @return true if geolocation is required, otherwise false
     */
    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    /**
     * Sets whether geolocation is required for the event.
     *
     * @param geolocationRequired true if geolocation is required, otherwise false
     */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /**
     * Gets the maximum number of entrants allowed for the event.
     *
     * @return the maximum number of entrants, or -1 if there is no limit
     */
    public int getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * Sets the maximum number of entrants for the event.
     *
     * @param maxEntrants the maximum number of entrants
     */
    public void setMaxEntrants(int maxEntrants) {
        this.maxEntrants = maxEntrants;
    }

    /**
     * Gets the start date of the event in milliseconds since the epoch.
     *
     * @return the start date of the event
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the event in milliseconds since the epoch.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the event in milliseconds since the epoch.
     *
     * @return the end date of the event
     */
    public long getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the event in milliseconds since the epoch.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the deadline for registration or participation in the event.
     *
     * @return the deadline in milliseconds since the epoch
     */
    public long getDeadline() {
        return deadline;
    }

    /**
     * Sets the deadline for registration or participation in the event.
     *
     * @param deadline the deadline to set in milliseconds
     */
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
}
