package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;

import java.util.concurrent.CompletableFuture;

public class ScanQrViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final EventRepository eventRepository;

    public ScanQrViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Scan QR Code");
        eventRepository = EventRepository.getInstance();
    }

    public CompletableFuture<Event> getEventByQrCodeHash(String qrCodeHash){
        return eventRepository.getEventByQrCodeHash(qrCodeHash);
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void setText(String text) {
        mText.setValue(text);
    }
}