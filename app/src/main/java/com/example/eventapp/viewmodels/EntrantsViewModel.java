package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.SignupFilter;
import com.example.eventapp.repositories.SignupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntrantsViewModel extends ViewModel {
    private Event currentEventToQuery;
    private final SignupRepository signupRepository;
    private final EventRepository eventRepository;
    private final MediatorLiveData<List<User>> filteredUsersLiveData = new MediatorLiveData<>();
    private LiveData<List<User>> currentUsersLiveData;
    private SignupFilter currentFilter;

    public EntrantsViewModel(){
        this(SignupRepository.getInstance(), EventRepository.getInstance());
    }

    public EntrantsViewModel(SignupRepository signupRepository, EventRepository eventRepository){
        this.signupRepository = signupRepository;
        this.eventRepository = eventRepository;
    }

    public LiveData<List<User>> getFilteredUsersLiveData() {
        return filteredUsersLiveData;
    }

    public void setCurrentEventToQuery(Event currentEventToQuery) {
        this.currentEventToQuery = currentEventToQuery;
        updateFilter(currentFilter != null ? currentFilter : new SignupFilter());
    }

    public void updateFilter(SignupFilter filter){
        currentFilter = filter;

        if (currentUsersLiveData != null) {
            filteredUsersLiveData.removeSource(currentUsersLiveData);
        }

        if (currentEventToQuery != null) {
            currentUsersLiveData = signupRepository.getSignedUpUsersByFilterLiveData(currentEventToQuery.getDocumentId(), filter);
            filteredUsersLiveData.addSource(currentUsersLiveData, filteredUsersLiveData::setValue);
        } else {
            Log.e("EntrantsViewModel", "Current event is null");
            filteredUsersLiveData.setValue(new ArrayList<>());
        }
    }

    public void clearFilter(){
        updateFilter(new SignupFilter());
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
}
