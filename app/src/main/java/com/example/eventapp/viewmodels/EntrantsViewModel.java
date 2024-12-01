package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.SignupFilter;
import com.example.eventapp.repositories.SignupRepository;

import java.util.ArrayList;
import java.util.List;

public class EntrantsViewModel extends ViewModel {
    private Event currentEventToQuery;
    private final SignupRepository signupRepository;
    private final MediatorLiveData<List<User>> filteredUsersLiveData = new MediatorLiveData<>();
    private LiveData<List<User>> currentUsersLiveData;
    private SignupFilter currentFilter;

    public EntrantsViewModel(){
        this(SignupRepository.getInstance());
    }

    public EntrantsViewModel(SignupRepository signupRepository){
        this.signupRepository = signupRepository;
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
}
