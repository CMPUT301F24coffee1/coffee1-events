package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.photos.PhotoManager;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final LiveData<User> currentUserLiveData;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final MediatorLiveData<List<Facility>> facilitiesLiveData = new MediatorLiveData<>();
    private Facility selectedFacility;
    private LiveData<List<Facility>> currentFacilitiesSource;

    /**
     * Initialization of the ViewModel, getting both userRepository and facilityRepository for later use
     * Then, it takes the current User's live data from the userRepository, which is what the ViewModel
     * primarily operates with, and sets a forever observer on it
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
     * Initializes ProfileViewModel, but allows you to pre-specify the repositories for testing purposes
     */
    public ProfileViewModel(UserRepository userRepository, FacilityRepository facilityRepository) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
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
                    Log.e(TAG, "Failed to update user: " + user.getName(), task.getException());
                }
            });
        }
    }

    /**
     * Loads the list of facilities into facilitiesLiveData to be used elsewhere in the ViewModel
     * @param userId The userId of the user being modified in the profile
     */
    private void loadFacilities(String userId) {
        if (currentFacilitiesSource != null) {
            facilitiesLiveData.removeSource(currentFacilitiesSource);
        }
        currentFacilitiesSource = facilityRepository.getFacilitiesOfOrganizerLiveData(userId);
        facilitiesLiveData.addSource(currentFacilitiesSource, facilitiesLiveData::setValue);
    }

    /**
     * Adds a facility to the repository, which then updates the database
     * @param facility The facility to add to the repository
     */
    public void addFacility(Facility facility) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            facility.setOrganizerId(user.getUserId());

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
     * If no facility is selected, it will run newSelectedFacility() instead
     * @return The currently selected Facility
     */
    public Facility getSelectedFacility() {
        if (this.selectedFacility != null) {
            return this.selectedFacility;
        }
        return newSelectedFacility();
    }

    /**
     * Creates a new facility, and then selects it for use in communication between fragments
     * @return The newly created facility
     */
    public Facility newSelectedFacility() {
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

    /**
     * Removes the profile photo of the user from firebase and from the user's data
     */
    public void removeUserPhoto() {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            Uri photoUri = user.getPhotoUri();
            PhotoManager.deletePhotoFromFirebase(photoUri);
            user.setPhotoUriString("");
            userRepository.saveUser(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Successfully removed photo from user: " + user.getName());
                } else {
                    Log.e(TAG, "Failed to remove photo from user: " + user.getName(), task.getException());
                }
            });
        }
    }

    /**
     * Removes the photo of the selected facility
     */
    public void removePhotoOfSelectedFacility() {
        Uri photoUri = selectedFacility.getPhotoUri();
        PhotoManager.deletePhotoFromFirebase(photoUri);
        selectedFacility.setPhotoUriString("");
        facilityRepository.updateFacility(selectedFacility);
    }

}
