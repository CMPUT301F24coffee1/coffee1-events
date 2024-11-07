package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Facility;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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

    public Task<DocumentReference> addFacility(Facility facility) {
        String organizerId = facility.getOrganizerId();
        if (organizerId == null) throw new NullPointerException("organizerId cannot be null");

        return facilityCollection.add(facility)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = task.getResult().getId();
                        facility.setDocumentId(documentId);
                        Log.d(TAG, "addFacility: success - ID: " + documentId);
                    } else {
                        Log.e(TAG, "addFacility: fail", task.getException());
                    }
                });
    }

    public Task<Void> updateFacility(Facility facility) {
        String documentId = facility.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return facilityCollection.document(documentId).set(facility)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateFacility: success - ID: " + documentId);
                    } else {
                        Log.e(TAG, "updateFacility: fail", task.getException());
                    }
                });
    }

    public Task<Void> removeFacility(Facility facility) {
        String documentId = facility.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return facilityCollection.document(documentId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "removeFacility: success - ID: " + documentId);
                    } else {
                        Log.e(TAG, "removeFacility: fail", task.getException());
                    }
                });
    }

    public LiveData<List<Facility>> getFacilitiesOfOrganizerLiveData(String organizerId) {
        MutableLiveData<List<Facility>> liveData = new MutableLiveData<>();
        Query query = facilityCollection.whereEqualTo("organizerId", organizerId);

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "getFacilitiesOfOrganizerLiveData: listen failed", e);
                liveData.setValue(new ArrayList<>());
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Facility> facilities = querySnapshot.toObjects(Facility.class);

                for (int i = 0; i < facilities.size(); i++) {
                    facilities.get(i).setDocumentId(querySnapshot.getDocuments().get(i).getId());
                }
                Log.d(TAG, "getFacilitiesOfOrganizerLiveData: success");
                liveData.setValue(facilities);
            } else {
                Log.d(TAG, "getFacilitiesOfOrganizerLiveData: no facilities found");
                liveData.setValue(new ArrayList<>());
            }
        });
        return liveData;
    }
}
