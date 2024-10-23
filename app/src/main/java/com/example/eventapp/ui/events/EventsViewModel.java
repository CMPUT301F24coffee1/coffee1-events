package com.example.eventapp.ui.events;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.Event;

import java.util.ArrayList;

public class EventsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<Event>> events;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Events");
    }

    public LiveData<String> getText() {
        return mText;
    }
}