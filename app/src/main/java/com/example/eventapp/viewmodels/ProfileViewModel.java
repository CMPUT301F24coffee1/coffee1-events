package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;

import java.util.Objects;

public class ProfileViewModel extends ViewModel {

    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    /**
     * Initialized the View Model, creating a new UserRepository
     * @param androidId The user's android device ID to find the user in the database
     */
    public ProfileViewModel(String androidId) {
        userRepository = new UserRepository();

        mUser = (MutableLiveData<User>) userRepository.getUserLiveData(androidId);
    }

    public LiveData<User> getUser() {
        return mUser;
    }

}
