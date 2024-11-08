package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Signup;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.repositories.UserRepository;

import java.util.List;


public class EventsViewModel extends ViewModel {

    private final String TAG = "EventsViewModel";
    private final MutableLiveData<String> mText;

    private final EventRepository eventRepository;
    private final SignupRepository signupRepository;
    private final LiveData<User> currentUserLiveData;

    private final MediatorLiveData<List<Event>> organizedEventsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Event>> signedUpEventsLiveData = new MediatorLiveData<>();

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Events");

        eventRepository = EventRepository.getInstance();
        signupRepository = SignupRepository.getInstance();

        UserRepository userRepository = UserRepository.getInstance();
        currentUserLiveData = userRepository.getCurrentUserLiveData();

        // load organized and signed-up events when current user data is available
        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                loadOrganizedEvents(user.getUserId());
                loadSignedUpEvents(user.getUserId());
            }
        });
    }

    public LiveData<List<Event>> getOrganizedEvents() {
        return organizedEventsLiveData;
    }

    public LiveData<List<Event>> getSignedUpEvents() {
        return signedUpEventsLiveData;
    }

    private void loadOrganizedEvents(String userId) {
        LiveData<List<Event>> organizedEvents = eventRepository.getEventsOfOrganizerLiveData(userId);
        organizedEventsLiveData.addSource(organizedEvents, organizedEventsLiveData::setValue);
    }

    private void loadSignedUpEvents(String userId) {
        LiveData<List<Event>> signedUpEvents = eventRepository.getSignedUpEventsOfUserLiveData(userId);
        signedUpEventsLiveData.addSource(signedUpEvents, signedUpEventsLiveData::setValue);
    }

    public void addEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            event.setOrganizerId(currentUser.getUserId());

            eventRepository.addEvent(event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Added event with name: " + event.getEventName());
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

    public void unregisterFromEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            signupRepository.removeSignup(currentUser.getUserId(), event.getDocumentId());
        }
    }

    public void registerToEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            signupRepository.addSignup(new Signup(currentUser.getUserId(), event.getDocumentId()));
        }
    }
    public LiveData<String> getText() {
        return mText;
    }
}