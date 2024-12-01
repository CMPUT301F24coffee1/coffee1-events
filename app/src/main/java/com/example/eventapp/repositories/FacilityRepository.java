package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.eventapp.models.Facility;
import com.example.eventapp.services.photos.PhotoManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The `FacilityRepository` class is a singleton repository responsible for managing Firestore
 * operations related to `Facility` data. This includes adding, updating, retrieving, and removing
 * facility records. The repository also provides LiveData-based methods for observing lists of
 * facilities, filtered by criteria such as organizer ID or retrieving all facilities.
 */
public class FacilityRepository {

    private static final String TAG = "FacilityRepository";
    private static FacilityRepository instance;
    private final CollectionReference facilityCollection;

    /**
     * Initializes a new instance of FacilityRepository with the default Firebase instance.
     */
    private FacilityRepository() {
        facilityCollection = FirebaseFirestore.getInstance().collection("facilities");
    }

    /**
     * Initializes a new instance of FacilityRepository with a specified Firestore test instance.
     *
     * @param testInstance The Firestore instance to use, used in tests.
     */
    private FacilityRepository(FirebaseFirestore testInstance) {
        facilityCollection = testInstance.collection("facilities");
    }

    /**
     * Retrieves the singleton instance of FacilityRepository.
     *
     * @return The singleton instance of FacilityRepository.
     */
    public static synchronized FacilityRepository getInstance() {
        if (instance == null) {
            instance = new FacilityRepository();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of FacilityRepository using a specified Firestore instance.
     *
     * @param testInstance The Firestore test instance to use.
     * @return A singleton test instance of FacilityRepository.
     */
    public static synchronized FacilityRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new FacilityRepository(testInstance);
        }
        return instance;
    }

    /**
     * Adds a new facility to Firestore.
     *
     * @param facility The facility to add.
     * @return A CompletableFuture containing the document ID of the newly added facility.
     * @throws NullPointerException if the facility or its organizerId is null.
     */
    public CompletableFuture<String> addFacility(Facility facility) {
        Objects.requireNonNull(facility);
        String organizerId = facility.getOrganizerId();

        CompletableFuture<String> future = new CompletableFuture<>();

        if (organizerId == null) {
            future.completeExceptionally(new NullPointerException("organizerId cannot be null"));
            return future;
        }

        facilityCollection.add(facility)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = task.getResult().getId();
                        facility.setDocumentId(documentId);
                        Log.d(TAG, "addFacility: success - ID: " + documentId);
                        future.complete(documentId);
                    } else {
                        Log.e(TAG, "addFacility: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Updates an existing facility in Firestore.
     *
     * @param facility The facility with updated information.
     * @return A CompletableFuture indicating the completion of the update.
     * @throws NullPointerException if the facility or its documentId is null.
     */
    public CompletableFuture<Void> updateFacility(Facility facility) {
        Objects.requireNonNull(facility);
        String documentId = facility.getDocumentId();

        CompletableFuture<Void> future = new CompletableFuture<>();

        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        facilityCollection.document(documentId).set(facility)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateFacility: success - ID: " + documentId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "updateFacility: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Removes a facility from Firestore.
     *
     * @param facility The facility to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     * @throws NullPointerException if the facility or its documentId is null.
     */
    public CompletableFuture<Void> removeFacility(Facility facility) {
        Objects.requireNonNull(facility);
        String documentId = facility.getDocumentId();

        CompletableFuture<Void> future = new CompletableFuture<>();
        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        facilityCollection.document(documentId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "removeFacility: success - ID: " + documentId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "removeFacility: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Retrieves a LiveData list of facilities organized by a specific user.
     *
     * @param organizerId The ID of the organizer.
     * @return LiveData containing a list of facilities organized by the specified user.
     */
    public LiveData<List<Facility>> getFacilitiesOfOrganizerLiveData(String organizerId) {
        Query query = facilityCollection.whereEqualTo("organizerId", organizerId);
        return Common.runQueryLiveData("getFacilitiesOfOrganizerLiveData", query, Facility.class, TAG);
    }

    /**
     * Retrieves a LiveData list of all existing facilities.
     *
     * @return LiveData containing a list of all facilities.
     */
    public LiveData<List<Facility>> getAllFacilitiesLiveData() {
        return Common.runQueryLiveData("getAllFacilitiesLiveData", facilityCollection, Facility.class, TAG);
    }
}
