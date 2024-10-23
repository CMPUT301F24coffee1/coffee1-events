package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminProfilesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AdminProfilesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Browse Profiles");
    }

    public LiveData<String> getText() {
        return mText;
    }
}