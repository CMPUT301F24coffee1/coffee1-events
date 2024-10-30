package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private static final String TAG = "EventRepository";
    private final CollectionReference eventCollection;

    public EventRepository() {
        eventCollection = FirebaseFirestore.getInstance().collection("events");
    }

    public Task<DocumentReference> addEvent(Event event) {
        String organizerId = event.getOrganizerId();
        String facilityId = event.getFacilityId();
        if (organizerId == null) {
            throw new NullPointerException("organizerId cannot be null");
        }
        if (facilityId == null) {
            Log.w(TAG, "addEvent: facilityId is null - event does not belong to any facility");
        }

        return eventCollection.add(event)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = task.getResult().getId();
                    event.setDocumentId(documentId);
                    Log.d(TAG, "addEvent: success - ID: " + documentId);
                } else {
                    Log.e(TAG, "addEvent: fail", task.getException());
                }
            });
    }

    public Task<Void> updateEvent(Event event) {
        String documentId = event.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return eventCollection.document(event.getDocumentId()).set(event)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "updateEvent: success - ID: " + documentId);
                } else {
                    Log.e(TAG, "updateEvent: fail", task.getException());
                }
            });
    }

    public Task<Void> removeEvent(Event event) {
        String documentId = event.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return eventCollection.document(event.getDocumentId()).delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "removeEvent: success - ID: " + documentId);
                } else {
                    Log.e(TAG, "removeEvent: fail", task.getException());
                }
            });
    }

    public LiveData<List<Event>> getEventsOfOrganizerLiveData(String organizerId) {
        Query query = eventCollection.whereEqualTo("organizerId", organizerId);

        return runQueryLiveData("getEventsOfOrganizerLiveData", query);
    }

    public LiveData<List<Event>> getEventsOfOrganizerLiveData(String organizerId, String facilityId) {
        Query query = eventCollection
                .whereEqualTo("organizerId", organizerId)
                .whereEqualTo("facilityId", facilityId);

        return runQueryLiveData("getEventsOfOrganizerLiveData", query);
    }

    public LiveData<List<Event>> getEventsOfFacilityLiveData(String facilityId) {
        Query query = eventCollection.whereEqualTo("facilityId", facilityId);

        return runQueryLiveData("getEventsOfFacilityLiveData", query);
    }

    private LiveData<List<Event>> runQueryLiveData(String methodName, Query query) {
        MutableLiveData<List<Event>> liveData = new MutableLiveData<>();

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "runQueryLiveData: " + methodName + ": listen failed", e);
                liveData.setValue(new ArrayList<>());
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Event> events = querySnapshot.toObjects(Event.class);

                for (int i = 0; i < events.size(); i++) {
                    events.get(i).setDocumentId(querySnapshot.getDocuments().get(i).getId());
                }
                Log.d(TAG, "runQueryLiveData: " + methodName + ": success");
                liveData.setValue(events);
            } else {
                Log.d(TAG, "runQueryLiveData: " + methodName + ": no documents found");
                liveData.setValue(new ArrayList<>());
            }
        });
        return liveData;
    }
}
