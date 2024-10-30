package com.example.eventapp.models;

import com.google.firebase.firestore.Exclude;

public class Facility {
    @Exclude
    private String documentId;
    private String organizerId;

    private String facilityName;
    private String facilityDescription;

    public Facility() {
       // empty constructor for firebase
    }

    public Facility(String facilityName) {
        this.facilityName = facilityName;
    }

    public Facility(String facilityName, String facilityDescription) {
        this.facilityName = facilityName;
        this.facilityDescription = facilityDescription;
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

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityDescription() {
        return facilityDescription;
    }

    public void setFacilityDescription(String facilityDescription) {
        this.facilityDescription = facilityDescription;
    }
}
