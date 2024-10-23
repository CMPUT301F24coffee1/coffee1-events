package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<ArrayList<Event>> eventsLiveData;
    private final ArrayList<Event> eventList;

    public EventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Events");

        eventList = new ArrayList<>();
        eventsLiveData = new MutableLiveData<>(eventList);

        // add test data
        eventList.add(new Event("Event 1", "Description 1"));
        eventList.add(new Event("Event 2", "Description 2"));
        eventList.add(new Event("Event 3", "Description 3"));
        eventsLiveData.setValue(eventList);
    }

    // TODO: temporary: boilerplate until firebase is integrated
    public MutableLiveData<ArrayList<Event>> getEvents() {
        return eventsLiveData;
    }

    // TODO: temporary: boilerplate until firebase is integrated
    public void addEvent(Event event) {
        eventList.add(event);
        eventsLiveData.setValue(new ArrayList<>(eventList));
    }

    // TODO: temporary: boilerplate until firebase is integrated
    public void removeEvent(Event event) {
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getEventName().equals(event.getEventName())) {
                eventList.remove(i);
                eventsLiveData.setValue(new ArrayList<>(eventList));
                break;
            }
        }
        eventsLiveData.setValue(new ArrayList<>(eventList));
    }

    // TODO: temporary: boilerplate until firebase is integrated
    public void updateEvent(Event updatedEvent) {
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getEventName().equals(updatedEvent.getEventName())) {
                eventList.set(i, updatedEvent);
                eventsLiveData.setValue(new ArrayList<>(eventList));
                break;
            }
        }
    }

    public LiveData<String> getText() {
        return mText;
    }
}