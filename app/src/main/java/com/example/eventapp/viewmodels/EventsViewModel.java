package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.UserRepository;

import java.util.List;


public class EventsViewModel extends ViewModel {

    private final String TAG = "EventsViewModel";
    private final MutableLiveData<String> mText;
    private final LiveData<User> currentUserLiveData;
    private final EventRepository eventRepository;

    private final MediatorLiveData<List<Event>> eventsLiveData = new MediatorLiveData<>();

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Events");

        eventRepository = new EventRepository();
        UserRepository userRepository = UserRepository.getInstance();
        currentUserLiveData = userRepository.getCurrentUserLiveData();

        // wait until currentUserLiveData emits a non-null value before loading events
        eventsLiveData.addSource(currentUserLiveData, user -> {
            if (user != null) {
                loadEventsForUser(user.getUserId());
            }
        });
    }

    public LiveData<List<Event>> getEvents() {
        return eventsLiveData;
    }

    private void loadEventsForUser(String userId) {
        LiveData<List<Event>> userEvents = eventRepository.getEventsOfOrganizerLiveData(userId);
        eventsLiveData.addSource(userEvents, eventsLiveData::setValue);
    }

    public void addEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            event.setOrganizerId(currentUser.getUserId());

            eventRepository.addEvent(event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Added event with name: " + event.getEventName());
                    event.setQrCodeHash(task.getResult().getId());
                    updateEvent(event);
                } else {
                    Log.e(TAG, "Failed to add event", task.getException());
                }
            });
        }
    }

    public void removeEvent(Event event) {
        eventRepository.removeEvent(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "Removed event with name: " + event.getEventName());
            } else {
                Log.e(TAG, "Failed to remove event", task.getException());
            }
        });
    }

    public void updateEvent(Event updatedEvent) {
        eventRepository.updateEvent(updatedEvent).addOnCompleteListener(task -> {
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