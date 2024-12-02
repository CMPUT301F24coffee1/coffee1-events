package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Facility;
import com.example.eventapp.models.Signup;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.photos.PhotoManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


/**
 * The EventsViewModel class is responsible for managing and organizing event-related data
 * and operations for the associated view in a structured MVVM pattern. This view model
 * communicates with repositories to perform CRUD operations on events, handle user signups,
 * and track events the user is organizing or attending. It also manages the status and
 * permissions of the current user in relation to events.
 */
public class EventsViewModel extends ViewModel {

    private final String TAG = "EventsViewModel";
    private final MutableLiveData<String> mText;

    private final EventRepository eventRepository;
    private final SignupRepository signupRepository;
    private final FacilityRepository facilityRepository;
    private final LiveData<User> currentUserLiveData;

    private final MediatorLiveData<List<Event>> organizedEventsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Event>> signedUpEventsLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<Facility>> userFacilitiesLiveData = new MediatorLiveData<>();

    /**
     * Default constructor for EventsViewModel.
     * Initializes the view model with default repositories for events, signups, facilities, and users.
     */
    public EventsViewModel() {
        this(
                EventRepository.getInstance(),
                SignupRepository.getInstance(),
                UserRepository.getInstance(),
                FacilityRepository.getInstance(),
                null
        );
    }

    /**
     * Parameterized constructor for EventsViewModel for dependency injection.
     *
     * @param eventRepository    The EventRepository instance.
     * @param signupRepository   The SignupRepository instance.
     * @param userRepository     The UserRepository instance.
     * @param facilityRepository The FacilityRepository instance.
     * @param injectedUserLiveData LiveData of the current user, allowing for test injection.
     */
    public EventsViewModel(
            EventRepository eventRepository,
            SignupRepository signupRepository,
            UserRepository userRepository,
            FacilityRepository facilityRepository,
            LiveData<User> injectedUserLiveData) {
        mText = new MutableLiveData<>();
        mText.setValue("Events");

        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
        this.facilityRepository = facilityRepository;

        // Dependency injection for tests
        if (injectedUserLiveData != null) {
            currentUserLiveData = injectedUserLiveData;
        } else {
            currentUserLiveData = userRepository.getCurrentUserLiveData();
        }

        // Load data when current user data is available
        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                loadOrganizedEvents(user.getUserId());
                loadSignedUpEvents(user.getUserId());
                loadUserFacilities(user.getUserId());
            }
        });
    }

    /**
     * Retrieves LiveData containing a list of events organized by the current user.
     *
     * @return LiveData of a list of organized events.
     */
    public LiveData<List<Event>> getOrganizedEvents() {
        return organizedEventsLiveData;
    }

    /**
     * Retrieves LiveData containing a list of events the user is signed up for.
     *
     * @return LiveData of a list of signed-up events.
     */
    public LiveData<List<Event>> getSignedUpEvents() {
        return signedUpEventsLiveData;
    }

    /**
     * Retrieves LiveData containing a list of facilities associated with the current user.
     *
     * @return LiveData of a list of user's facilities.
     */
    public LiveData<List<Facility>> getUserFacilities() {
        return userFacilitiesLiveData;
    }

    /**
     * Loads events organized by the specified user and updates the corresponding LiveData.
     *
     * @param userId The ID of the user whose organized events are to be loaded.
     */
    private void loadOrganizedEvents(String userId) {
        LiveData<List<Event>> organizedEvents = eventRepository.getEventsOfOrganizerLiveData(userId);
        organizedEventsLiveData.addSource(organizedEvents, organizedEventsLiveData::setValue);
    }

    /**
     * Loads events the specified user is signed up for and updates the corresponding LiveData.
     *
     * @param userId The ID of the user whose signed-up events are to be loaded.
     */
    private void loadSignedUpEvents(String userId) {
        LiveData<List<Event>> signedUpEvents = eventRepository.getSignedUpEventsOfUserLiveData(userId);
        signedUpEventsLiveData.addSource(signedUpEvents, signedUpEventsLiveData::setValue);
    }

    /**
     * Loads facilities associated with the specified user and updates the corresponding LiveData.
     *
     * @param userId The ID of the user whose facilities are to be loaded.
     */
    private void loadUserFacilities(String userId) {
        LiveData<List<Facility>> userFacilities = facilityRepository.getFacilitiesOfOrganizerLiveData(userId);
        userFacilitiesLiveData.addSource(userFacilities, userFacilitiesLiveData::setValue);
    }

    /**
     * Retrieves LiveData containing a list of signups for a specific event.
     *
     * @param eventId The ID of the event.
     * @return LiveData of a list of signups for the event.
     */
    public LiveData<List<Signup>> getSignupsOfEvent(String eventId) {
        return signupRepository.getSignupsOfEventLiveData(eventId);
    }

    /**
     * Adds a new event and assigns the current user as the organizer.
     *
     * @param event The event to add.
     * @return A CompletableFuture containing the event's document ID.
     */
    public CompletableFuture<String> addEvent(Event event) {
        CompletableFuture<String> future = new CompletableFuture<>();

        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            processEventWithUser(currentUser, event)
                    .thenAccept(future::complete)
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        } else {
            future.completeExceptionally(new Exception("User is not yet fetched"));
        }
        return future;
    }

    /**
     * Processes an event with a user, assigning the user as the event's organizer.
     *
     * @param currentUser The current user organizing the event.
     * @param event The event to be processed.
     * @return A CompletableFuture containing the event's document ID.
     */
    private CompletableFuture<String> processEventWithUser(User currentUser, Event event) {
        event.setOrganizerId(currentUser.getUserId());

        return eventRepository.addEvent(event)
                .thenCompose(documentId -> handleEventAdded(event, documentId))
                .exceptionally(throwable -> {
                    Log.e(TAG, "addEvent: failed to add event", throwable);
                    throw new CompletionException(throwable);
                });
    }

    /**
     * Updates the event with a QR code hash after it has been added to the database.
     *
     * @param event The event to be updated with a QR code hash.
     * @param documentId The document ID of the added event.
     * @return A CompletableFuture containing the event's document ID after the update.
     */
    private CompletableFuture<String> handleEventAdded(Event event, String documentId) {
        event.setDocumentId(documentId);
        event.setQrCodeHash(documentId + "--display");

        return updateEvent(event)
                .thenApply(discard -> {
                    Log.i(TAG, "Added event with name: " + event.getEventName());
                    return documentId;
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "addEvent: failed to update event with qrCodeHash", throwable);
                    throw new CompletionException(throwable);
                });
    }

    /**
     * Removes an event from the database.
     *
     * @param event The event to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     */
    public CompletableFuture<Void> removeEvent(Event event) {
        if (event.hasPoster()) {
            PhotoManager.deletePhotoFromFirebase(event.getPosterUri());
        }

        CompletableFuture<Void> removeEventFuture = eventRepository.removeEvent(event);

        removeEventFuture.thenAccept(discard -> {
            Log.i(TAG, "Removed event with name: " + event.getEventName());
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to remove event", throwable);
            return null;
        });
        return removeEventFuture;
    }

    /**
     * Updates an existing event in the database.
     *
     * @param event The event with updated information.
     * @return A CompletableFuture indicating the completion of the update.
     */
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

    /**
     * Registers the current user for a specified event.
     *
     * @param event The event to register for.
     * @return A CompletableFuture containing the document ID of the new signup.
     */
    public CompletableFuture<String> registerToEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            return signupRepository.addSignup(new Signup(currentUser.getUserId(), event.getDocumentId()));
        }
        return null;
    }

    /**
     * Registers the current user for a specified event.
     *
     * @param event The event to register for.
     * @return A CompletableFuture containing the document ID of the new signup.
     */
    public CompletableFuture<String> registerToEvent(Event event, double lat, double lon) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            return signupRepository.addSignup(new Signup(currentUser.getUserId(), event.getDocumentId(), lat, lon));
        }
        return null;
    }

    /**
     * Unregisters the current user from a specified event.
     *
     * @param event The event to unregister from.
     * @return A CompletableFuture indicating the completion of the unregistration.
     */
    public CompletableFuture<Void> unregisterFromEvent(Event event) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            return signupRepository.removeSignup(currentUser.getUserId(), event.getDocumentId());
        }
        return null;
    }

    /**
     * Checks if the current user is signed up for a specific event.
     *
     * @param event The event to check for signup status.
     * @return True if the user is signed up; otherwise, false.
     */
    public boolean isSignedUp(Event event){
        List<Event> eventsList = signedUpEventsLiveData.getValue();
        if(eventsList == null){
            return false;
        }
        for (int i = 0; i < eventsList.size(); i++){
            String hash = eventsList.get(i).getQrCodeHash();
            if( hash != null && hash.equals(event.getQrCodeHash()) ){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user has organizer or admin privileges.
     *
     * @return True if the user is an organizer or admin; otherwise, false.
     */
    public boolean isUserOrganizerOrAdmin(){
        User currentUser = currentUserLiveData.getValue();
        return currentUser != null && (currentUser.isAdmin() || currentUser.isOrganizer());
    }

    /**
     * Checks if the current user has permission to edit a specific event.
     *
     * @param event The event to check for edit permission.
     * @return True if the user can edit the event; otherwise, false.
     */
    public boolean canEdit(Event event){
        User currentUser = currentUserLiveData.getValue();
        if (currentUser == null) {
            return false;
        }
        // check if correct organizer
        return currentUser.isAdmin() ||
                (currentUser.isOrganizer() && currentUser.getUserId().equals(event.getOrganizerId()));
    }

    /**
     * Retrieves the LiveData text label associated with this view model.
     *
     * @return LiveData containing the text label.
     */
    public LiveData<String> getText() {
        return mText;
    }
}