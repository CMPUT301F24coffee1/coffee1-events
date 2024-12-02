package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Notification;
import com.example.eventapp.repositories.DTOs.SignupFilter;
import com.example.eventapp.repositories.DTOs.UserSignupEntry;
import com.example.eventapp.repositories.NotificationRepository;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel responsible for managing data and operations related to entrants for a specific event.
 * It handles filtering entrants, managing QR code hashes, sending notifications, and drawing entrants for an event.
 */
public class EntrantsViewModel extends ViewModel {
    private static final String TAG = "EntrantsViewModel";

    private Event currentEventToQuery;
    private final SignupRepository signupRepository;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final FirebaseFunctions firebaseFunctions;

    private final MediatorLiveData<List<UserSignupEntry>> filteredUserSignupEntriesLiveData = new MediatorLiveData<>();
    private LiveData<List<UserSignupEntry>> currentUserSignupEntriesLiveData;
    private SignupFilter currentFilter;

    /**
     * Default constructor for EntrantsViewModel.
     * Initializes repositories and Firebase Functions with singleton instances.
     */
    public EntrantsViewModel(){
        this(
                SignupRepository.getInstance(),
                NotificationRepository.getInstance(),
                EventRepository.getInstance(),
                FirebaseFunctions.getInstance());
    }

    /**
     * Constructor for EntrantsViewModel with DI.
     *
     * @param signupRepository       The repository for managing signups.
     * @param notificationRepository The repository for managing notifications.
     * @param eventRepository        The repository for managing events.
     * @param firebaseFunctions      Firebase Functions instance for calling cloud functions.
     */
    public EntrantsViewModel(
            SignupRepository signupRepository,
            NotificationRepository notificationRepository,
            EventRepository eventRepository,
            FirebaseFunctions firebaseFunctions) {
        this.signupRepository = signupRepository;
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
        this.firebaseFunctions = firebaseFunctions;
    }

    /**
     * Gets the LiveData containing the filtered list of UserSignupEntry objects.
     *
     * @return LiveData containing the filtered list of entrants.
     */
    public LiveData<List<UserSignupEntry>> getFilteredUserSignupEntriesLiveData() {
        return filteredUserSignupEntriesLiveData;
    }

    /**
     * Set the event for which the user is currently viewing entrant details for.
     *
     * @param currentEventToQuery
     */
    public void setCurrentEventToQuery(Event currentEventToQuery) {
        this.currentEventToQuery = currentEventToQuery;
        updateFilter(currentFilter != null ? currentFilter : new SignupFilter());
    }

    /**
     * Update the criteria for the entrants that are displayed in the ViewEntrantsFragment.
     *
     * @param filter
     */
    public void updateFilter(SignupFilter filter){
        currentFilter = filter;

        if (currentUserSignupEntriesLiveData != null) {
            filteredUserSignupEntriesLiveData.removeSource(currentUserSignupEntriesLiveData);
        }

        if (currentEventToQuery != null) {
            currentUserSignupEntriesLiveData = signupRepository.getSignedUpUsersByFilterLiveData(currentEventToQuery.getDocumentId(), filter);
            filteredUserSignupEntriesLiveData.addSource(currentUserSignupEntriesLiveData, filteredUserSignupEntriesLiveData::setValue);
        } else {
            Log.e("EntrantsViewModel", "Current event is null");
            filteredUserSignupEntriesLiveData.setValue(new ArrayList<>());
        }
    }

    /**
     * Sends a notification to entrants.
     *
     * @param selectedEntrants the entrants that will receive the notification
     * @param messageContent the contents of the notification
     */
    public void notifyEntrants(List<UserSignupEntry> selectedEntrants, String messageContent) {
        String notificationTitle = "Notification for Event \"" +currentEventToQuery.getEventName()+ "\"";
        for(UserSignupEntry userSignupEntry: selectedEntrants) {
            String userId = userSignupEntry.getUser().getUserId();
            Notification notification = new Notification(userId, notificationTitle, messageContent);
            notificationRepository.uploadNotification(notification);
        }
    }

    /**
     * Removes a signup entry for a specific user and event.
     *
     * @param userSignupEntry The signup entry to remove.
     * @return A CompletableFuture indicating the success or failure of the removal.
     */
    public CompletableFuture<Void> removeSignupEntry(UserSignupEntry userSignupEntry) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (currentEventToQuery == null) {
            Exception ex = new IllegalStateException("Current event is null");
            future.completeExceptionally(ex);
            return future;
        }
        String userId = userSignupEntry.getUser().getUserId();
        String eventId = currentEventToQuery.getDocumentId();

        return signupRepository.getSignup(userId, eventId)
                .thenCompose(signup -> {
                    if (signup == null) {
                        Exception ex = new IllegalStateException("Signup not found");
                        future.completeExceptionally(ex);
                        return future;
                    }
                    return signupRepository.removeSignup(signup);
                });
    }

    /**
     * Cancels the signups of the selected entrants.
     *
     * @param selectedEntrants The entrants to cancel.
     */
    public void cancelEntrants(List<UserSignupEntry> selectedEntrants){
        for(UserSignupEntry userSignupEntry: selectedEntrants) {
            removeSignupEntry(userSignupEntry);
        }
    }

    /**
     * Gets the current event being queried.
     *
     * @return The current event being queried.
     */
    public Event getCurrentEventToQuery() {
        return currentEventToQuery;
    }

    /**
     * Used to delete the QRCodeHash data from the database.
     *
     * @return
     */
    public CompletableFuture<Void> deleteQrCodeHash() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (currentEventToQuery == null) {
            Exception ex = new IllegalStateException("Current event is null");
            future.completeExceptionally(ex);
            return future;
        }
        String eventId = currentEventToQuery.getDocumentId();

        return eventRepository.getEventById(eventId)
                .thenCompose(event -> {
                    if (event == null) {
                        Exception ex = new IllegalStateException("Event not found");
                        future.completeExceptionally(ex);
                        return future;
                    }
                    event.setQrCodeHash(null);
                    return eventRepository.updateEvent(event);
                });
    }

    /**
     * Used to re-add QRCodeHash data to the database
     * @return
     */
    public CompletableFuture<Void> reAddQrCodeHash() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (currentEventToQuery == null) {
            Exception ex = new IllegalStateException("Current event is null");
            future.completeExceptionally(ex);
            return future;
        }
        String eventId = currentEventToQuery.getDocumentId();
        String newHash = eventId + "--display";

        return eventRepository.getEventById(eventId)
                .thenCompose(event -> {
                    if (event == null) {
                        Exception ex = new IllegalStateException("Event not found");
                        future.completeExceptionally(ex);
                        return future;
                    }
                    event.setQrCodeHash(newHash);
                    return eventRepository.updateEvent(event);
                });
    }

    /**
     * Used to draw entrants from the waitling list (that will be invited to enroll in the event)
     * @param drawCount
     * @return
     */
    public CompletableFuture<String> drawEntrants(int drawCount) {
        Log.d("EntrantsViewModel", "drawEntrants called for " + drawCount + " Entrants");
        CompletableFuture<String> future = new CompletableFuture<>();

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", currentEventToQuery.getDocumentId());
        data.put("organizerId", currentEventToQuery.getOrganizerId());
        data.put("numberOfAttendees", drawCount);

        firebaseFunctions
                .getHttpsCallable("runLottery")
                .call(data)
                .continueWith(task -> {
                    HashMap<String, String> result = (HashMap) task.getResult().getData();

                    if (result == null) {
                        future.completeExceptionally(new NullPointerException("Lottery result is null"));
                        return future;
                    }
                    Log.i(TAG, "Lottery result: " + result.get("result"));
                    future.complete(result.get("result"));
                    return null;
                }).addOnFailureListener(ex -> {
                    Log.e(TAG, "Running the lottery failed:", ex);
                    future.completeExceptionally(ex);
                });

        return future;
    }
}
