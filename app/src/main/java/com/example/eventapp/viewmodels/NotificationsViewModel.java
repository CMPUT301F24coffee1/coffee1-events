package com.example.eventapp.viewmodels;

import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;

import java.util.concurrent.CompletableFuture;

public class NotificationsViewModel {
    private final EventRepository eventRepository;

    /**
     * Default constructor for NotificationsViewModel.
     */
    public NotificationsViewModel() {
        this(EventRepository.getInstance());
    }

    /**
     * Constructor used for DI in tests.
     */
    public NotificationsViewModel(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves an event based on its QR code hash.
     *
     * @param eventId The hash associated with the event's QR code.
     * @return A CompletableFuture containing the event associated with the QR code.
     */
    public CompletableFuture<Event> getEventById(String eventId){
        return eventRepository.getEventById(eventId);
    }
}
