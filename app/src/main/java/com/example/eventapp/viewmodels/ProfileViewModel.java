package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final LiveData<User> currentUserLiveData;
    private final UserRepository userRepository;

    /**
     * Initialized the View Model, creating a new UserRepository
     */
    public ProfileViewModel() {
        userRepository = UserRepository.getInstance();
        currentUserLiveData = userRepository.getCurrentUserLiveData();
    }

    public LiveData<User> getUser() {
        return currentUserLiveData;
    }

    public void updateUser(String name, String email, String phone) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            userRepository.saveUser(user);
        }
    }

}
