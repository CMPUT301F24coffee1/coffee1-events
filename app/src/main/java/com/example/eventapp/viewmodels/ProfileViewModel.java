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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.Objects;

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
        this(UserRepository.getInstance(), FacilityRepository.getInstance(), null);
    }

    /**
     * Initializes ProfileViewModel, but allows you to pre-specify the repositories for testing purposes
     */
    public ProfileViewModel(UserRepository userRepository, FacilityRepository facilityRepository, LiveData<User> injectedUserLiveData) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;

        if (injectedUserLiveData != null) {
            currentUserLiveData = injectedUserLiveData;
        } else {
            currentUserLiveData = userRepository.getCurrentUserLiveData();
        }

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
    public Task<Void> updateUser(String name, String email, String phone, boolean optNotifs, boolean isOrganizer, String photoUriString) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            user.setNotificationOptOut(optNotifs);
            user.setOrganizer(isOrganizer);
            user.setPhotoUriString(photoUriString);

            Task<Void> updateUserTask = userRepository.saveUser(user);
            updateUserTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Updated user with name: " + user.getName());
                } else {
                    Log.e(TAG, "Failed to update user: " + user.getName(), task.getException());
                }
            });
            return updateUserTask;
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
    public Task<String> addFacility(Facility facility) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            facility.setOrganizerId(user.getUserId());

            Task<DocumentReference> addFacilityTask = facilityRepository.addFacility(facility);
            TaskCompletionSource<String> documentIdTask = new TaskCompletionSource<>();

            addFacilityTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String documentId = task.getResult().getId();
                    facility.setDocumentId(documentId);
                    documentIdTask.setResult(documentId);
                    Log.i(TAG, "Added facility with name: " + facility.getFacilityName());
                } else {
                    documentIdTask.setException(Objects.requireNonNull(task.getException()));
                    Log.e(TAG, "Failed to add facility", task.getException());
                }
            });
            return documentIdTask.getTask();
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
    public Task<Void> updateSelectedFacility(Facility facility) {
        Task<Void> updateFacilityTask = facilityRepository.updateFacility(facility);

        updateFacilityTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "Updated facility with name: " + facility.getFacilityName());
            } else {
                Log.e(TAG, "Failed to update facility", task.getException());
            }
        });
        return updateFacilityTask;
    }

    /**
     * Removes the currently selected Facility from the repository (from getSelectedFacility())
     */
    public Task<Void> removeSelectedFacility() {
        Facility facility = getSelectedFacility();
        if (facility != null) {
            Task<Void> removeFacilityTask = facilityRepository.removeFacility(facility);

            removeFacilityTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Removed facility with name: " + facility.getFacilityName());
                } else {
                    Log.e(TAG, "Failed to remove facility", task.getException());
                }
            });
            return removeFacilityTask;
        }
        return null;
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
