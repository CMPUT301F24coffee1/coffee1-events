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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class EventsViewModel extends ViewModel {

    private final String TAG = "EventsViewModel";
    private final MutableLiveData<String> mText;

    private final EventRepository eventRepository;
    private final SignupRepository signupRepository;
    private final LiveData<User> currentUserLiveData;

    private final MediatorLiveData<List<Event>> organizedEventsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Event>> signedUpEventsLiveData = new MediatorLiveData<>();

    public EventsViewModel() {
        this(
                EventRepository.getInstance(),
                SignupRepository.getInstance(),
                UserRepository.getInstance(),
                null
        );
    }

    public EventsViewModel(
            EventRepository eventRepository,
            SignupRepository signupRepository,
            UserRepository userRepository,
            LiveData<User> injectedUserLiveData) {
        mText = new MutableLiveData<>();
        mText.setValue("Events");

        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;

        // dependency injection for tests
        if (injectedUserLiveData != null) {
            currentUserLiveData = injectedUserLiveData;
        } else {
            currentUserLiveData = userRepository.getCurrentUserLiveData();
        }

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

    public Task<String> addEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            event.setOrganizerId(currentUser.getUserId());

            Task<DocumentReference> addEventTask = eventRepository.addEvent(event);
            TaskCompletionSource<String> documentIdTask = new TaskCompletionSource<>();

            addEventTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = task.getResult().getId();
                    event.setDocumentId(documentId);
                    documentIdTask.setResult(documentId);
                    Log.i(TAG, "Added event with name: " + event.getEventName());
                } else {
                    documentIdTask.setException(Objects.requireNonNull(task.getException()));
                    Log.e(TAG, "Failed to add event", task.getException());
                }
            });
            return documentIdTask.getTask();
        }
        return null;
    }

    public Task<Void> removeEvent(Event event) {
        Task<Void> removeEventTask = eventRepository.removeEvent(event);

        removeEventTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "Removed event with name: " + event.getEventName());
            } else {
                Log.e(TAG, "Failed to remove event", task.getException());
            }
        });
        return removeEventTask;
    }

    public Task<Void> updateEvent(Event updatedEvent) {
        Task<Void> updateEventTask = eventRepository.updateEvent(updatedEvent);

        updateEventTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "Updated event with name: " + updatedEvent.getEventName());
            } else {
                Log.e(TAG, "Failed to update event", task.getException());
            }
        });
        return updateEventTask;
    }

    public Task<DocumentReference> registerToEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            return signupRepository.addSignup(new Signup(currentUser.getUserId(), event.getDocumentId()));
        }
        return null;
    }

    public CompletableFuture<Void> unregisterFromEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            return signupRepository.removeSignup(currentUser.getUserId(), event.getDocumentId());
        }
        return null;
    }

    public boolean isSignedUp(Event event){
        List<Event> eventsList = signedUpEventsLiveData.getValue();
        if(eventsList == null){
            return false;
        }
        for (Event e : eventsList){
            if(e.getDocumentId().equals(event.getDocumentId())){
                return true;
            }
        }
        return false;
    }

    public LiveData<String> getText() {
        return mText;
    }
}