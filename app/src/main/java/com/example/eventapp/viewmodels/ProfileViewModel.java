package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final LiveData<User> currentUserLiveData;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final MediatorLiveData<List<Facility>> facilitiesLiveData = new MediatorLiveData<>();
    private Facility selectedFacility;

    /**
     * Initialized the View Model, creating a new UserRepository
     */
    public ProfileViewModel() {
        userRepository = UserRepository.getInstance();
        facilityRepository = FacilityRepository.getInstance();
        currentUserLiveData = userRepository.getCurrentUserLiveData();

        // load organized and signed-up events when current user data is available
        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                 loadFacilities(user.getUserId());
            }
        });
    }

    /**
     * Gets the live data from the user
     * @return Live data from the user
     */
    public LiveData<User> getUser() {
        return currentUserLiveData;
    }

    /**
     * Gets the live data of the list of facilities a user manages
     * @return Live data of hte list of facilities a user manages
     */
    public LiveData<List<Facility>> getFacilities() {
        return facilitiesLiveData;
    }

    /**
     * Updates a user in the user repository with new information
     * @param name The new name of the user
     * @param email The new email of the user
     * @param phone The new phone number of the user
     * @param optNotifs Whether or not the user opts out of notifications
     * @param isOrganizer Whether or not the user is an organizer
     * @param photoUriString The Uri string of the newly updated photo
     */
    public void updateUser(String name, String email, String phone, boolean optNotifs, boolean isOrganizer, String photoUriString) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            user.setNotificationOptOut(optNotifs);
            user.setOrganizer(isOrganizer);
            user.setPhotoUriString(photoUriString);
            userRepository.saveUser(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Updated user with name: " + user.getName());
                } else {
                    Log.e(TAG, "Failed to update user", task.getException());
                }
            });
        }
    }

    /**
     * Makes sure the list of facilities the user owns is actually loaded
     * @param userId The userId of the user being modified in the profile
     */
    public void loadFacilities(String userId) {
        LiveData<List<Facility>> facilities = facilityRepository.getFacilitiesOfOrganizerLiveData(userId);
        facilitiesLiveData.addSource(facilities, facilitiesLiveData::setValue);
    }

    /**
     * Adds a facility to the repository, which then updates the database
     * @param facility The facility to add to the repository
     */
    public void addFacility(Facility facility) {
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            facility.setOrganizerId(currentUser.getUserId());

            facilityRepository.addFacility(facility).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Added facility with name: " + facility.getFacilityName());
                } else {
                    Log.e(TAG, "Failed to add facility", task.getException());
                }
            });
        }
    }

    /**
     * Sets the currently selected Facility, for use in communication between fragments
     * @param facility The Facility to select
     */
    public void setSelectedFacility(Facility facility) {
        this.selectedFacility = facility;
    }

    /**
     * Gets the currently selected Facility, for use in communication between fragments
     * If no facility is selected, it will make an empty facility and select and return that instead
     * @return The currently selected Facility
     */
    public Facility getSelectedFacility() {
        if (this.selectedFacility != null) {
            return this.selectedFacility;
        }
        Facility facility = new Facility("");
        setSelectedFacility(facility);
        return facility;
    }

    /**
     * Updates the currently selected Facility to the repository (from getSelectedFacility())
     */
    public void updateSelectedFacility(Facility facility) {
        facilityRepository.updateFacility(facility);
    }

    /**
     * Removes the currently selected Facility from the repository (from getSelectedFacility())
     */
    public void removeSelectedFacility() {
        Facility facility = getSelectedFacility();
        facilityRepository.removeFacility(facility);
    }

}
