package com.example.eventapp.interfaces;

/**
 * Interface for entities that have a Firestore document ID.
 * Provides methods to set a unique document ID for each instance.
 */
public interface HasDocumentId {
    /**
     * Sets the unique document ID for the Firestore document.
     *
     * @param documentId the unique document ID to set
     */
    void setDocumentId(String documentId);
}
