package com.example.eventapp.viewmodels;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.photos.PhotoManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private Uri selectedImageUri;
    private String selectedName;
    private Object selectedObject;
    private boolean imageRemoved;

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

    /**
     * Sets the selected image, for use in communication between fragments.
     * @param imageUri Uri of the image to select
     */
    public void setSelectedImage(Uri imageUri) {
        this.selectedImageUri = imageUri;
        // Ensure these values are initialized.
        this.selectedObject = null;
        this.selectedName = "";
        this.imageRemoved = false;
    }

    /**
     * Finds the object that the image belongs to, if it exists, by querying the database
     * with the specified imageUri
     * @param imageUri The imageUri of the currently selected object
     */
    public CompletableFuture<Boolean> setSelectedObject(Uri imageUri) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        switch (Objects.requireNonNull(imageUri.getLastPathSegment()).split("/")[0]) {
            case "events":
                eventRepository.getEventByImageUri(imageUri).thenAccept(event -> {
                    this.selectedObject = event;
                    if (event != null) {
                        // Manually specifying the name is because every object has a different
                        // get name function
                        this.selectedName = event.getEventName();
                        future.complete(true);
                    } else {
                        Log.e(TAG, "Event not found with selected Image Uri: " + imageUri);
                        this.selectedName = "Event Not Found";
                        future.complete(null);
                    }
                }).exceptionally(throwable -> {
                    Log.e(TAG, "Event not found with selected Image Uri: " + imageUri, throwable);
                    this.selectedObject = null;
                    this.selectedName = "Event Not Found";
                    future.complete(null);
                    return null;
                });
                break;
            case "profiles":
                userRepository.getUserByImageUri(imageUri).thenAccept(user -> {
                    this.selectedObject = user;
                    if (user != null) {
                        // Manually specifying the name is because every object has a different
                        // get name function
                        this.selectedName = user.getName();
                        future.complete(true);
                    } else {
                        Log.e(TAG, "User not found with selected Image Uri: " + imageUri);
                        this.selectedName = "User Not Found";
                        future.complete(null);
                    }
                }).exceptionally(throwable -> {
                    Log.e(TAG, "User not found with selected Image Uri: " + imageUri, throwable);
                    this.selectedObject = null;
                    this.selectedName = "User Not Found";
                    future.complete(null);
                    return null;
                });
                break;
            case "facilities":
                facilityRepository.getFacilityByImageUri(imageUri).thenAccept(facility -> {
                    this.selectedObject = facility;
                    if (facility != null) {
                        // Manually specifying the name is because every object has a different
                        // get name function
                        this.selectedName = facility.getFacilityName();
                        future.complete(true);
                    } else {
                        Log.e(TAG, "Facility not found with selected Image Uri: " + imageUri);
                        this.selectedName = "Facility Not Found";
                        future.complete(null);
                    }
                }).exceptionally(throwable -> {
                    Log.e(TAG, "Facility not found with selected Image Uri: " + imageUri, throwable);
                    this.selectedObject = null;
                    this.selectedName = "Facility Not Found";
                    future.complete(null);
                    return null;
                });
                break;
            default:
                Log.e(TAG, "Invalid image type for selected image");
                this.selectedObject = null;
                this.selectedName = "Object Not Found";
                future.complete(null);
                break;
        }

        return future;
    }

    /**
     * Gets the selected image, for use in communication between fragments
     * @return The Uri of the currently selected image
     */
    public Uri getSelectedImage() {
        return this.selectedImageUri;
    }

    /**
     * Gets the selected name, found at selection time by querying the database for the
     * object in which the selected image belongs
     * @return The name of the selected image
     */
    public String getSelectedName() {
        return this.selectedName;
    }

    /**
     * Removes the currently selected image, and, if an image object exists,
     * removes the association with this image
     */
    public void removeSelectedImage() {
        this.imageRemoved = true;
        PhotoManager.deletePhotoFromFirebase(selectedImageUri);
        if (selectedObject instanceof Event) {
            eventRepository.getEventById(((Event) selectedObject).getDocumentId()).thenAccept(event -> {
                event.setPosterUriString("");
                eventRepository.updateEvent(event);
            }).exceptionally(throwable -> {
                Log.e(TAG, "removeSelectedImage: Failed to find event with ID: " + ((Event) selectedObject).getDocumentId(), throwable);
                return null;
            });
        } else if (selectedObject instanceof User) {
            userRepository.getUser(((User) selectedObject).getUserId())
                .thenAccept(user -> {
                    if (user != null) {
                        user.setPhotoUriString("");
                        userRepository.saveUser(user);
                    } else {
                        Log.e(TAG, "removeSelectedImage: Failed to find user with ID: " + ((User) selectedObject).getUserId());
                    }
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "removeSelectedImage: Failed to find user with ID: " + ((User) selectedObject).getUserId());
                    return null;
                });
        } else if (selectedObject instanceof Facility) {
            facilityRepository.getFacilityById(((Facility) selectedObject).getDocumentId()).thenAccept(facility -> {
                facility.setPhotoUriString("");
                facilityRepository.updateFacility(facility);
            }).exceptionally(throwable -> {
                Log.e(TAG, "removeSelectedImage: Failed to find facility with ID: " + ((Facility) selectedObject).getDocumentId(), throwable);
                return null;
            });
        }
    }

    /**
     * Gets the current image removed status
     * @return Whether or not the currently selected image was removed
     */
    public boolean getImageRemoved() {
        return this.imageRemoved;
    }

}
