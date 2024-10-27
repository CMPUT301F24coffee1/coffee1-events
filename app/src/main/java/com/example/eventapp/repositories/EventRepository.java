package com.example.eventapp.repositories;

import android.util.Log;

import com.example.eventapp.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventRepository {

    private static final String TAG = "EventRepository";
    private final CollectionReference eventCollection;

    public EventRepository() {
        eventCollection = FirebaseFirestore.getInstance().collection("events");
    }

    public Task<DocumentReference> addEvent(Event event) {
        return eventCollection.add(event)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = task.getResult().getId();
                    event.setDocumentId(documentId);
                    Log.d(TAG, "addEvent: success - ID: " + documentId);
                } else {
                    Log.e(
                            TAG,
                            "addEvent: fail",
                            task.getException()
                    );
                }
            });
    }

    public Task<Void> updateEvent(Event event) {
        return eventCollection.document(event.getDocumentId()).set(event)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = event.getDocumentId();
                    Log.d(TAG, "updateEvent: success - ID: " + documentId);
                } else {
                    Log.e(
                            TAG,
                            "updateEvent: fail",
                            task.getException()
                    );
                }
            });
    }

    public Task<Void> removeEvent(Event event) {
        return eventCollection.document(event.getDocumentId()).delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = event.getDocumentId();
                    Log.d(TAG, "removeEvent: success - ID: " + documentId);
                } else {
                    Log.e(
                            TAG,
                            "removeEvent: fail",
                            task.getException()
                    );
                }
            });
    }

    public CompletableFuture<List<Event>> getEventsForOrganizer(String organizerId) {
        CompletableFuture<List<Event>> future = new CompletableFuture<>();
        Query query = eventCollection.whereEqualTo("organizerId", organizerId);

        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<Event> events = querySnapshot.toObjects(Event.class);

                    for (int i = 0; i < events.size(); i++) {
                        events.get(i).setDocumentId(querySnapshot.getDocuments().get(i).getId());
                    }
                    future.complete(events);
                    Log.d(TAG, "getEventsForOrganizer: success");
                } else {
                    future.completeExceptionally(task.getException());
                    Log.e(
                            TAG,
                            "getEventsForOrganizer: fail",
                            task.getException()
                    );
                }
            });
        return future;
    }
}
