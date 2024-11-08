package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.photos.PhotoManager;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileViewModel extends ViewModel {

    private LiveData<User> currentUserLiveData;
    private final LiveData<User> actualUserLiveData;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final MediatorLiveData<List<Facility>> facilitiesLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<User>> usersLiveData = new MediatorLiveData<>();
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
        actualUserLiveData = userRepository.getCurrentUserLiveData();
        currentUserLiveData = actualUserLiveData;
        usersLiveData.addSource(userRepository.getAllUsersLiveData(), usersLiveData::setValue);

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
    public ProfileViewModel(
            @NonNull UserRepository userRepository,
            FacilityRepository facilityRepository,
            MutableLiveData<User> injectedLiveData) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;

        if (injectedLiveData == null) {
            actualUserLiveData = userRepository.getCurrentUserLiveData();
        } else {
            actualUserLiveData = injectedLiveData;
        }
        currentUserLiveData = actualUserLiveData;
        usersLiveData.addSource(userRepository.getAllUsersLiveData(), usersLiveData::setValue);

        // load facilities when user data is available
        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                loadFacilities(user.getUserId());
            }
        });
    }

    /**
     * Deletes the currently selected User
     */
    public void deleteSelectedUser() {
        userRepository.removeUser(currentUserLiveData.getValue());
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
     * Gets the live data of the list of profiles
     * @return Live data of the list of profiles
     */
    public LiveData<List<User>> getUsers() {
        return usersLiveData;
    }

    /**
     * Sets the currently selected user
     * @param user User that is now being selected
     */
    public void setSelectedUser(User user) {
        currentUserLiveData = userRepository.getUserLiveData(user.getUserId());
        loadFacilities(user.getUserId()); // Load the new facilities from the selected user
    }

    /**
     * Returns the actual user the device is logged in as
     * @return The user the device is logged in as
     */
    public LiveData<User> getActualUser() {
        return actualUserLiveData;
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
    public CompletableFuture<Void> updateUser(String name, String email, String phone, boolean optNotifs, boolean isOrganizer, String photoUriString) {
        User user = currentUserLiveData.getValue();

        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            user.setNotificationOptOut(optNotifs);
            user.setOrganizer(isOrganizer);
            user.setPhotoUriString(photoUriString);

            CompletableFuture<Void> future = userRepository.saveUser(user);

            future.thenAccept(discard -> {
                Log.i(TAG, "Updated user with name: " + user.getName());
            }).exceptionally(throwable -> {
                Log.e(TAG, "Failed to update user: " + user.getName(), throwable);
                return null;
            });
            return future;
        }
        return null;
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
    public CompletableFuture<String> addFacility(Facility facility) {
        User user = currentUserLiveData.getValue();

        if (user != null) {
            facility.setOrganizerId(user.getUserId());
            CompletableFuture<String> future = facilityRepository.addFacility(facility);

            future.thenAccept(documentId -> {
                Log.i(TAG, "Added facility with name: " + facility.getFacilityName());
            }).exceptionally(throwable -> {
                Log.e(TAG, "Failed to add facility", throwable);
                return null;
            });
            return future;
        }
        return null;
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
    public CompletableFuture<Void> updateSelectedFacility(Facility facility) {
        return facilityRepository.updateFacility(facility);
    }

    /**
     * Removes the currently selected Facility from the repository (from getSelectedFacility())
     */
    public CompletableFuture<Void> removeSelectedFacility() {
        Facility facility = getSelectedFacility();
        return facilityRepository.removeFacility(facility);
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

            userRepository.saveUser(user).thenAccept(discard -> {
                Log.i(TAG, "Successfully removed photo from user: " + user.getName());
            }).exceptionally(throwable -> {
                Log.e(TAG, "Failed to remove photo from user: " + user.getName(), throwable);
                return null;
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
