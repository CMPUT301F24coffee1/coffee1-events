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
import com.example.eventapp.repositories.SignupRepository;

import java.util.ArrayList;
import java.util.List;

public class EntrantsViewModel extends ViewModel {
    private Event currentEventToQuery;
    private final SignupRepository signupRepository;
    private final NotificationRepository notificationRepository;
    private final MediatorLiveData<List<UserSignupEntry>> filteredUserSignupEntriesLiveData = new MediatorLiveData<>();
    private LiveData<List<UserSignupEntry>> currentUserSignupEntriesLiveData;
    private SignupFilter currentFilter;

    public EntrantsViewModel(){
        this(SignupRepository.getInstance(), NotificationRepository.getInstance());
    }

    public EntrantsViewModel(SignupRepository signupRepository, NotificationRepository notificationRepository){
        this.signupRepository = signupRepository;
        this.notificationRepository = notificationRepository;
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

    public void clearFilter(){
        updateFilter(new SignupFilter());
    }

    public Event getCurrentEventToQuery() {
        return currentEventToQuery;
    }
}
