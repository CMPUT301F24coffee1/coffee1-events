package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ImagesViewModel is a View Model that can list all images in the storage database. It manages
 * the admin view for images, and can pull a list of event images, profile images, or facility images
 * from the storage database.
 */
public class ImagesViewModel extends ViewModel {
    private final FirebaseStorage storage;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;

    /**
     * Initializes the Images View model by default, specifying the events, users, and facilities repositories,
     * and then calls the more verbose function that allows you to specify repositories. This is to allow
     * for custom repositories for testing purposes
     */
    public ImagesViewModel() {
        this(EventRepository.getInstance(), UserRepository.getInstance(), FacilityRepository.getInstance());

    }

    /**
     * Initializes the ViewModel with events, users, and facilities as repositories. It also makes a storage
     * reference for the firebase storage. This is so that we can find in the vent, user, or facility repository,
     * a event, user, or facility for a specified image.
     * @param eventRepository Repository that contains all events
     * @param userRepository Repository that contains all users
     * @param facilityRepository Repository that contains all facilities
     */
    public ImagesViewModel(EventRepository eventRepository, UserRepository userRepository, FacilityRepository facilityRepository) {
        storage = FirebaseStorage.getInstance();
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
    }

    public CompletableFuture<List<StorageReference>> getImages(String type) {

        CompletableFuture<List<StorageReference>> future = new CompletableFuture<>();

        StorageReference eventImagesRef = storage.getReference().child(type);
        eventImagesRef.listAll()
            .addOnSuccessListener(listResult -> {
                List<StorageReference> imageUriList = new ArrayList<>(listResult.getPrefixes());
                Log.i(TAG, "Successfully gathered all images of type: " + type);
                future.complete(imageUriList);
            })
            .addOnFailureListener(throwable -> {
                Log.e(TAG, "Failed to get eventImages", throwable);
                future.completeExceptionally(throwable);
            });
        return future;
    }

}
