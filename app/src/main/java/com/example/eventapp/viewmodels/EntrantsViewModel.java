package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.SignupFilter;
import com.example.eventapp.repositories.SignupRepository;

public class EntrantsViewModel extends ViewModel {
    private Event currentEventToQuery;
    private final SignupRepository signupRepository;

    public EntrantsViewModel(){
        this(SignupRepository.getInstance());
    }

    public EntrantsViewModel(SignupRepository signupRepository){
        this.signupRepository = signupRepository;
    }

    public void updateFilter(SignupFilter filter){
        // update filter
        Log.d("EntrantsViewModel", "updateFilter called");
    }

    public void clearFilter(){
        // clear filter
        Log.d("EntrantsViewModel", "clearFilter called");
    }

    public Event getCurrentEventToQuery() {
        return currentEventToQuery;
    }

    public void setCurrentEventToQuery(Event currentEventToQuery) {
        this.currentEventToQuery = currentEventToQuery;
    }
}
