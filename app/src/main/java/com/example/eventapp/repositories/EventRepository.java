package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Signup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The `EventRepository` class is a singleton repository that provides access to Firestore operations
 * for managing `Event` data, including adding, updating, retrieving, and removing event records.
 * It also provides LiveData-based methods for observing lists of events filtered by criteria
 * such as user signups, organizer ID, facility ID, and all available events.
 */
public class EventRepository {

    private static final String TAG = "EventRepository";
    private static EventRepository instance;
    private final CollectionReference eventCollection;
    private final SignupRepository signupRepository;

    /**
     * Initializes a new instance of EventRepository with the default Firebase instance.
     */
    private EventRepository() {
        eventCollection = FirebaseFirestore.getInstance().collection("events");
        signupRepository = SignupRepository.getInstance();
    }

    /**
     * Initializes a new instance of EventRepository with a specified Firestore test instance.
     *
     * @param testInstance The Firestore instance to use, used in tests.
     */
    private EventRepository(FirebaseFirestore testInstance) {
        eventCollection = testInstance.collection("events");
        signupRepository = SignupRepository.getTestInstance(testInstance);
    }

    /**
     * Retrieves the singleton instance of EventRepository.
     *
     * @return The singleton instance of EventRepository.
     */
    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of EventRepository using a specified Firestore instance.
     *
     * @param testInstance The Firestore test instance to use.
     * @return A singleton test instance of EventRepository.
     */
    public static synchronized EventRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new EventRepository(testInstance);
        }
        return instance;
    }

    /**
     * Adds a new event to Firestore.
     *
     * @param event The event to add.
     * @return A CompletableFuture containing the document ID of the newly added event.
     * @throws NullPointerException if the event or its organizerId is null.
     */
    public CompletableFuture<String> addEvent(Event event) {
        Objects.requireNonNull(event);
        String organizerId = event.getOrganizerId();
        String facilityId = event.getFacilityId();
        CompletableFuture<String> future = new CompletableFuture<>();

        if (organizerId == null) {
            future.completeExceptionally(new NullPointerException("organizerId cannot be null"));
            return future;
        }
        if (facilityId == null) {
            Log.w(TAG, "addEvent: facilityId is null - event does not belong to any facility");
        }

        eventCollection.add(event)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = task.getResult().getId();
                        event.setDocumentId(documentId);
                        Log.d(TAG, "addEvent: success - ID: " + documentId);
                        future.complete(documentId);
                    } else {
                        Log.e(TAG, "addEvent: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Updates an existing event in Firestore.
     *
     * @param event The event with updated information.
     * @return A CompletableFuture indicating the completion of the update.
     * @throws NullPointerException if the event or its documentId is null.
     */
    public CompletableFuture<Void> updateEvent(Event event) {
        Objects.requireNonNull(event);
        String documentId = event.getDocumentId();
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        eventCollection.document(documentId).set(event)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateEvent: success - ID: " + documentId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "updateEvent: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Removes an event from Firestore.
     *
     * @param event The event to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     * @throws NullPointerException if the event or its documentId is null.
     */
    public CompletableFuture<Void> removeEvent(Event event) {
        Objects.requireNonNull(event);
        String documentId = event.getDocumentId();
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        eventCollection.document(documentId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "removeEvent: success - ID: " + documentId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "removeEvent: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Retrieves an event by its QR code hash.
     *
     * @param qrCodeHash The QR code hash to search for.
     * @return A CompletableFuture containing the event matching the QR code hash, or null if not found.
     */
    public CompletableFuture<Event> getEventByQrCodeHash(String qrCodeHash) {
        CompletableFuture<Event> future = new CompletableFuture<>();

        eventCollection
                .whereEqualTo("qrCodeHash", qrCodeHash)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setDocumentId(document.getId());
                        }
                        future.complete(event);
                    } else {
                        Log.w(TAG, "getEventByQrCodeHash: no event found");
                        future.complete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getEventByQrCodeHash: failed to retrieve event", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    /**
     * Retrieves a LiveData list of events that a user has signed up for.
     *
     * @param userId The ID of the user.
     * @return LiveData containing a list of events the user is signed up for.
     */
    public LiveData<List<Event>> getSignedUpEventsOfUserLiveData(String userId) {
        MutableLiveData<List<Event>> signedUpEventsLiveData = new MutableLiveData<>();

        signupRepository.getSignupsOfUserLiveData(userId).observeForever(signups -> {
            if (signups == null || signups.isEmpty()) {
                signedUpEventsLiveData.setValue(new ArrayList<>());
                return;
            }

            List<String> eventIds = new ArrayList<>();
            for (Signup signup : signups) {
                eventIds.add(signup.getEventId());
            }

            eventCollection.whereIn(FieldPath.documentId(), eventIds).get()
                .addOnCompleteListener(eventTask -> {
                    if (eventTask.isSuccessful() && eventTask.getResult() != null) {
                        List<Event> events = eventTask.getResult().toObjects(Event.class);

                        for (int i = 0; i < events.size(); i++) {
                            events.get(i).setDocumentId(eventTask.getResult().getDocuments().get(i).getId());
                        }
                        signedUpEventsLiveData.setValue(events);
                        Log.d(TAG, "getSignedUpEventsOfUserLiveData: retrieved " + events.size() + " events for user ID: " + userId);
                    } else {
                        Log.e(TAG, "getSignedUpEventsOfUserLiveData: failed to retrieve events", eventTask.getException());
                        signedUpEventsLiveData.setValue(new ArrayList<>());
                    }
                });
        });
        return signedUpEventsLiveData;
    }

    /**
     * Retrieves a LiveData list of events organized by a specific user.
     *
     * @param organizerId The ID of the organizer.
     * @return LiveData containing a list of events organized by the specified user.
     */
    public LiveData<List<Event>> getEventsOfOrganizerLiveData(String organizerId) {
        Query query = eventCollection.whereEqualTo("organizerId", organizerId);

        return Common.runQueryLiveData("getEventsOfOrganizerLiveData", query, Event.class, TAG);
    }

    /**
     * Retrieves a LiveData list of events organized by a specific user for a specific facility.
     *
     * @param organizerId The ID of the organizer.
     * @param facilityId The ID of the facility.
     * @return LiveData containing a list of events organized by the specified user at the specified facility.
     */
    public LiveData<List<Event>> getEventsOfOrganizerLiveData(String organizerId, String facilityId) {
        Query query = eventCollection
                .whereEqualTo("organizerId", organizerId)
                .whereEqualTo("facilityId", facilityId);

        return Common.runQueryLiveData("getEventsOfOrganizerLiveData", query, Event.class, TAG);
    }

    /**
     * Retrieves a LiveData list of events associated with a specific facility.
     *
     * @param facilityId The ID of the facility.
     * @return LiveData containing a list of events associated with the specified facility.
     */
    public LiveData<List<Event>> getEventsOfFacilityLiveData(String facilityId) {
        Query query = eventCollection.whereEqualTo("facilityId", facilityId);

        return Common.runQueryLiveData("getEventsOfFacilityLiveData", query, Event.class, TAG);
    }

    /**
     * Retrieves a LiveData list of all existing events.
     *
     * @return LiveData containing a list of all events.
     */
    public LiveData<List<Event>> getAllExistingEventsLiveData() {
        return Common.runQueryLiveData("getAllEventsLiveData", eventCollection, Event.class, TAG);
    }
}
