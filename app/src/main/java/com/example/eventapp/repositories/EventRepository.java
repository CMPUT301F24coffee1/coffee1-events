package com.example.eventapp.repositories;

import android.util.Log;

import com.example.eventapp.models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventRepository {

    private final CollectionReference eventCollection = FirebaseFirestore
            .getInstance()
            .collection("events");

    // TODO: Boilerplate
    public CompletableFuture<List<Event>> getEventsForOrganizer(String organizerDeviceId) {
        CompletableFuture<List<Event>> future = new CompletableFuture<>();

        Query query = eventCollection.whereEqualTo("organizerDeviceId", organizerDeviceId);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> events = task.getResult().toObjects(Event.class);
                        future.complete(events);
                        Log.d("EventRepository", "getEventsForOrganizer: success");
                    } else {
                        future.completeExceptionally(task.getException());
                        Log.e(
                                "EventRepository",
                                "getEventsForOrganizer: fail",
                                task.getException()
                        );
                    }
                });

        return future;
    }
}
