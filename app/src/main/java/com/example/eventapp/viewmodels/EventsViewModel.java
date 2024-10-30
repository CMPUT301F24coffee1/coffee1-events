package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class EventsViewModel extends ViewModel {

    private final String TEST_ID = "testId";
    private final String TAG = "EventsViewModel";
    private final MutableLiveData<String> mText;
    private final EventRepository eventRepository;
    private LiveData<List<Event>> eventsLiveData;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Events");
        eventRepository = new EventRepository();
    }

    public LiveData<List<Event>> getEvents() {
        if (eventsLiveData == null) {
            eventsLiveData = eventRepository.getEventsOfOrganizerLiveData(TEST_ID);
        }
        if (eventsLiveData == null) {
            Log.e(TAG, "Failed to fetch LiveData from repository");
        }

        // addEvent(new Event("Test 1", "Description 1"));
        // addEvent(new Event("Test 2", "Description 2"));
        // addEvent(new Event("Test 3", "Description 3"));
        return eventsLiveData;
    }

    public void addEvent(Event event) {
        event.setOrganizerId(TEST_ID);
        Task<DocumentReference> documentReference = eventRepository.addEvent(event);
        documentReference
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Added event with name: " + event.getEventName());
                    } else {
                        Log.e(TAG, "Failed to add event", task.getException());
                    }
                });
        documentReference.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                event.setQrCodeHash(documentReference.getId());
                updateEvent(event);
            }
        });
    }

    public void removeEvent(Event event) {
        eventRepository.removeEvent(event)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Removed event with name: " + event.getEventName());
                    } else {
                        Log.e(TAG, "Failed to remove event", task.getException());
                    }
                });
    }

    public void updateEvent(Event updatedEvent) {
        eventRepository.updateEvent(updatedEvent)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Updated event with name: " + updatedEvent.getEventName());
                    } else {
                        Log.e(TAG, "Failed to update event", task.getException());
                    }
                });
    }

    public LiveData<String> getText() {
        return mText;
    }
}