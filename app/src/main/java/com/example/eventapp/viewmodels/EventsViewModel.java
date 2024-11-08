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

    public CompletableFuture<String> addEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        CompletableFuture<String> addEventFuture = new CompletableFuture<>();

        if (currentUser != null) {
            event.setOrganizerId(currentUser.getUserId());

            CompletableFuture<String> repositoryFuture = eventRepository.addEvent(event);

            repositoryFuture.thenAccept(documentId -> {
                event.setDocumentId(documentId);
                addEventFuture.complete(documentId);
                Log.i(TAG, "Added event with name: " + event.getEventName());
            }).exceptionally(throwable -> {
                addEventFuture.completeExceptionally(throwable);
                Log.e(TAG, "addEvent: failed to add event to repository", throwable);
                return null;
            });
        } else {
            addEventFuture.completeExceptionally(new Exception("User is not yet fetched"));
        }
        return addEventFuture;
    }

    public CompletableFuture<Void> removeEvent(Event event) {
        CompletableFuture<Void> removeEventFuture = eventRepository.removeEvent(event);

        removeEventFuture.thenAccept(discard -> {
            Log.i(TAG, "Removed event with name: " + event.getEventName());
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to remove event", throwable);
            return null;
        });
        return removeEventFuture;
    }

    public CompletableFuture<Void> updateEvent(Event event) {
        CompletableFuture<Void> updateEventFuture = eventRepository.updateEvent(event);

        updateEventFuture.thenAccept(discard -> {
            Log.i(TAG, "Updated event with name: " + event.getEventName());
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to update event", throwable);
            return null;
        });
        return updateEventFuture;
    }

    public CompletableFuture<String> registerToEvent(Event event) {
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
        for (int i = 0; i < eventsList.size(); i++){
            if(eventsList.get(i).getQrCodeHash().equals(event.getQrCodeHash())){
                return true;
            }
        }
        return false;
    }

    public boolean isUserOrganizerOrAdmin(){
        User currentUser = currentUserLiveData.getValue();
        return currentUser != null && (currentUser.isAdmin() || currentUser.isOrganizer());
    }

    public boolean canEdit(Event event){
        User currentUser = currentUserLiveData.getValue();
        if (currentUser == null) {
            return false;
        }
        // check if correct organizer
        return currentUser.isAdmin() ||
                (currentUser.isOrganizer() && currentUser.getUserId().equals(event.getOrganizerId()));
    }

    public CompletableFuture<Event> getEventByQrCodeHash(String qrCodeHash){
        return eventRepository.getEventByQrCodeHash(qrCodeHash);
    }

    public LiveData<String> getText() {
        return mText;
    }
}