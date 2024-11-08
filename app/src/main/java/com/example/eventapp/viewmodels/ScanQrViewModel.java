package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;

import java.util.concurrent.CompletableFuture;

/**
 * ViewModel for handling QR code scanning functionality in the application.
 * This class interacts with the {@link EventRepository} to retrieve event data
 * based on a scanned QR code hash. It also maintains a LiveData text label
 * that can be used to display instructions or feedback to the user.
 */
public class ScanQrViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final EventRepository eventRepository;

    /**
     * Default constructor for ScanQrViewModel.
     */
    public ScanQrViewModel() {
        this(EventRepository.getInstance());
    }

    /**
     * Constructor used for DI in tests.
     */
    public ScanQrViewModel(EventRepository eventRepository) {
        mText = new MutableLiveData<>();
        mText.setValue("Scan QR Code");
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves an event based on its QR code hash.
     *
     * @param qrCodeHash The hash associated with the event's QR code.
     * @return A CompletableFuture containing the event associated with the QR code.
     */
    public CompletableFuture<Event> getEventByQrCodeHash(String qrCodeHash){
        return eventRepository.getEventByQrCodeHash(qrCodeHash);
    }

    /**
     * Retrieves the LiveData text label associated with this view model.
     *
     * @return LiveData containing the text label.
     */
    public LiveData<String> getText() {
        return mText;
    }

    /**
     * Sets the text label for this view model.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        mText.setValue(text);
    }
}