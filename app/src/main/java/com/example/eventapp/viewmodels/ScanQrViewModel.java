package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScanQrViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ScanQrViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Scan QR Code");
    }

    public LiveData<String> getText() {
        return mText;
    }
}