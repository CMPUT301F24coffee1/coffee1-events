package com.example.eventapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private MutableLiveData<User> mUser;
    private final String androidId;

    /**
     * Initialized the View Model, creating a new UserRepository
     * @param androidId The user's android device ID to find the user in the database
     */
    public ProfileViewModel(String androidId) {
        userRepository = new UserRepository();
        this.androidId = androidId;
    }

    public LiveData<User> getUser() {
        if (mUser == null) {
            mUser = (MutableLiveData<User>) userRepository.getUserLiveData(androidId);
        }
        return mUser;
    }

    public void updateUser(String name, String email, String phone) {
        if (mUser == null) {
            mUser = (MutableLiveData<User>) userRepository.getUserLiveData(androidId);
        }
        User user = mUser.getValue();
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            userRepository.saveUser(user);
        }
    }

}
