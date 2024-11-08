package com.example.eventapp.models;

import android.net.Uri;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Exclude;

/**
 * Represents a Facility that hosts events, including details such as name, description, and photo.
 * Implements the {@link HasDocumentId} interface for Firestore integration.
 */
public class Facility implements HasDocumentId {
    @Exclude
    private String documentId;
    private String organizerId;

    private String photoUriString = "";
    private String facilityName;
    private String facilityDescription;

    public Facility() {
       // empty constructor for firebase
    }

    /**
     * Creates a Facility with a specified name.
     *
     * @param facilityName the name of the facility
     */
    public Facility(String facilityName) {
        this.facilityName = facilityName;
    }

    public Facility(String facilityName, String facilityDescription) {
        this.facilityName = facilityName;
        this.facilityDescription = facilityDescription;
    }

    /**
     * Gets the Firestore document ID for the facility.
     *
     * @return the document ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the Firestore document ID for the facility.
     *
     * @param documentId the new document ID for the facility.
     */
    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the organizer device ID associated with this facility.
     *
     * @return the organizer device ID
     */
    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns the name of the facility.
     *
     * @return the facility name
     */
    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * Gets the description of the facility.
     *
     * @return the facility description
     */
    public String getFacilityDescription() {
        return facilityDescription;
    }

    /**
     * Sets the description for the facility.
     *
     * @param facilityDescription the description to set for the facility
     */
    public void setFacilityDescription(String facilityDescription) {
        this.facilityDescription = facilityDescription;
    }

    /**
     * Gets the URI string of the facility's photo.
     *
     * @return the URI string of the facility's photo
     */
    public String getPhotoUriString() {
        return photoUriString;
    }

    /**
     * Sets the URI string of the facility's photo.
     *
     * @param photoUriString the URI string to set for the facility's photo
     */
    public void setPhotoUriString(String photoUriString) {this.photoUriString = photoUriString;}

    /**
     * Gets the URI of the facility's photo.
     *
     * @return a URI pointing to the facility's photo, or null if none exists
     */
    public Uri getPhotoUri() { return Uri.parse(photoUriString);}

    /**
     * Determines if the facility has an associated photo.
     *
     * @return true if a photo exists, otherwise false
     */
    public Boolean hasPhoto() { return !photoUriString.isEmpty(); }
}
