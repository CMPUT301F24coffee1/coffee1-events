package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Facility;
import com.example.eventapp.photos.PhotoManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FacilityRepository {

    private static final String TAG = "FacilityRepository";
    private static FacilityRepository instance;
    private final CollectionReference facilityCollection;

    private FacilityRepository() {
        facilityCollection = FirebaseFirestore.getInstance().collection("facilities");
    }

    private FacilityRepository(FirebaseFirestore testInstance) {
        facilityCollection = testInstance.collection("facilities");
    }

    public static synchronized FacilityRepository getInstance() {
        if (instance == null) {
            instance = new FacilityRepository();
        }
        return instance;
    }

    public static synchronized FacilityRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new FacilityRepository(testInstance);
        }
        return instance;
    }

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

    public CompletableFuture<Void> removeFacility(Facility facility) {
        Objects.requireNonNull(facility);
        String documentId = facility.getDocumentId();

        CompletableFuture<Void> future = new CompletableFuture<>();
        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        if (facility.hasPhoto()) {
            PhotoManager.deletePhotoFromFirebase(facility.getPhotoUri());
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

    public LiveData<List<Facility>> getFacilitiesOfOrganizerLiveData(String organizerId) {
        Query query = facilityCollection.whereEqualTo("organizerId", organizerId);
        return Common.runQueryLiveData("getFacilitiesOfOrganizerLiveData", query, Facility.class, TAG);
    }

    public LiveData<List<Facility>> getAllFacilitiesLiveData() {
        return Common.runQueryLiveData("getAllFacilitiesLiveData", facilityCollection, Facility.class, TAG);
    }
}
