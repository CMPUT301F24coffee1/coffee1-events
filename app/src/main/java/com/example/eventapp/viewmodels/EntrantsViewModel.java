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


    public EntrantsViewModel(){
        this(
                SignupRepository.getInstance(),
                NotificationRepository.getInstance(),
                EventRepository.getInstance(),
                FirebaseFunctions.getInstance());
    }

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

    public LiveData<List<UserSignupEntry>> getFilteredUserSignupEntriesLiveData() {
        return filteredUserSignupEntriesLiveData;
    }

    public void setCurrentEventToQuery(Event currentEventToQuery) {
        this.currentEventToQuery = currentEventToQuery;
        updateFilter(currentFilter != null ? currentFilter : new SignupFilter());
    }

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

    public void notifyEntrants(List<UserSignupEntry> selectedEntrants, String messageContent) {
        String notificationTitle = "Notification for Event \"" +currentEventToQuery.getEventName()+ "\"";
        for(UserSignupEntry userSignupEntry: selectedEntrants) {
            String userId = userSignupEntry.getUser().getUserId();
            Notification notification = new Notification(userId, notificationTitle, messageContent);
            notificationRepository.uploadNotification(notification);
        }
    }

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

    public void cancelEntrants(List<UserSignupEntry> selectedEntrants){
        for(UserSignupEntry userSignupEntry: selectedEntrants) {
            removeSignupEntry(userSignupEntry);
        }
    }

    public Event getCurrentEventToQuery() {
        return currentEventToQuery;
    }

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
