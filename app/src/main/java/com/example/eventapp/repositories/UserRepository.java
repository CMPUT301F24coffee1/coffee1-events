package com.example.eventapp.repositories;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


/**
 * The `UserRepository` class is a singleton repository responsible for handling Firestore operations
 * related to `User` data management. This includes adding, updating, retrieving, and removing user records.
 * The repository also manages the current user’s real-time data updates via LiveData and provides
 * methods to retrieve lists of users as LiveData, enabling dynamic UI updates as data changes in Firestore.
 * It includes utility functions for setting the current user and retrieving a user’s unique identifier.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static UserRepository instance;
    private final CollectionReference userCollection;

    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private String currentUserId = null;
    private ListenerRegistration currentListenerRegistration = null;

    /**
     * Initializes a new instance of UserRepository with the default Firebase instance.
     */
    private UserRepository() {
        userCollection = FirebaseFirestore.getInstance().collection("users");
    }

    /**
     * Initializes a new instance of UserRepository with a specified Firestore test instance.
     *
     * @param testInstance The Firestore instance for testing.
     */
    private UserRepository(FirebaseFirestore testInstance) {
        userCollection = testInstance.collection("users");
    }

    /**
     * Retrieves the singleton instance of UserRepository.
     *
     * @return The singleton instance of UserRepository.
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of UserRepository using a specified Firestore instance.
     *
     * @param testInstance The Firestore instance for testing.
     * @return A singleton test instance of UserRepository.
     */
    public static synchronized UserRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new UserRepository(testInstance);
        }
        return instance;
    }

    /**
     * Gets the current user's LiveData.
     *
     * @return LiveData representing the current user.
     */
    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    /**
     * Sets the current user and updates the LiveData with real-time updates.
     *
     * @param user The user to set as the current user.
     */
    public void setCurrentUser(User user) {
        String userId = getUserIdOrThrow(user);
        Log.d(TAG, "setCurrentUser: setting current user to user with ID: " + userId);

        if (userId.equals(currentUserId)) {
            Log.w(TAG, "setCurrentUser: user is already the current user - ID: " + userId);
            return;
        }
        currentUserId = userId;

        if (currentListenerRegistration != null) {
            Log.d(TAG, "setCurrentUser: removing old listener");
            currentListenerRegistration.remove();
        }
        currentListenerRegistration = setUserLiveData(currentUserLiveData, userId);
    }

    /**
     * Saves a user document to Firestore.
     *
     * @param user The user to save.
     * @return A CompletableFuture indicating the completion of the save operation.
     * @throws NullPointerException if user or userId is null.
     */
    public CompletableFuture<Void> saveUser(User user) {
        String userId = getUserIdOrThrow(user);
        CompletableFuture<Void> future = new CompletableFuture<>();

        userCollection.document(userId).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUser: success for user with ID: " + userId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "saveUser: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Removes a user document from Firestore.
     *
     * @param user The user to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     * @throws NullPointerException if user or userId is null.
     */
    public CompletableFuture<Void> removeUser(User user) {
        String userId = getUserIdOrThrow(user);
        return removeUser(userId);
    }

    /**
     * Removes a user document from Firestore based on userId.
     *
     * @param userId The ID of the user to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     * @throws NullPointerException if userId is null.
     * @throws InvalidParameterException if userId matches the current logged-in user's ID.
     */
    public CompletableFuture<Void> removeUser(String userId) {
        if (userId == null) { throw new NullPointerException("userId cannot be null - set deviceId"); }
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (userId.equals(currentUserId)) {
            future.completeExceptionally(new InvalidParameterException("cannot remove current logged in user"));
            return future;
        }

        userCollection.document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "removeUser: success for user with ID: " + userId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "removeUser: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Retrieves a user document by userId.
     *
     * @param userId The ID of the user to retrieve.
     * @return A CompletableFuture containing the user document, or null if not found.
     */
    public CompletableFuture<User> getUser(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();

        userCollection.document(userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        Log.d(TAG, "getUser: success for user with ID: " + userId);
                    } else {
                        Log.e(TAG, "getUser: user is null after deserialization");
                    }
                    future.complete(user);
                }
                else if (task.isSuccessful() && !task.getResult().exists()) {
                    Log.d(TAG, "getUser: user does not exist for ID: " + userId);
                    future.complete(null);
                }
                else {
                    Log.e(TAG, "getUser: fail", task.getException());
                    future.completeExceptionally(task.getException());
                }
            });
        return future;
    }

    /**
     * Retrieves the userId from a user object or throws an exception if not set.
     *
     * @param user The user object to retrieve the userId from.
     * @return The userId if present.
     * @throws NullPointerException if user or userId is null.
     */
    private String getUserIdOrThrow(User user) throws NullPointerException {
        if (user == null) throw new NullPointerException("user cannot be null");
        String userId = user.getUserId();
        if (userId == null) throw new NullPointerException("userId cannot be null - set deviceId");
        return userId;
    }

    /**
     * Retrieves a user by its image uri.
     *
     * @param imageUri The Uri of the image to search for.
     * @return A CompletableFuture containing the user matching the image Uri hash, or null if not found.
     */
    public CompletableFuture<User> getUserByImageUri(Uri imageUri) {
        Objects.requireNonNull(imageUri);
        CompletableFuture<User> future = new CompletableFuture<>();

        userCollection
                .whereEqualTo("photoUri", imageUri)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setUserId(document.getId());
                        }
                        future.complete(user);
                    } else {
                        Log.w(TAG, "getUserByUri: no user found with Image Uri: " + imageUri);
                        future.complete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getUserByUri: failed to retrieve user with Image Uri: " + imageUri, e);
                    future.completeExceptionally(e);
                });
        return future;
    }

    /**
     * Sets up real-time updates for a user's LiveData by adding a snapshot listener to the user's document.
     *
     * @param liveData The LiveData object to update with user data.
     * @param userId The ID of the user to listen for updates on.
     * @return The ListenerRegistration for removing the listener if needed.
     */
    private ListenerRegistration setUserLiveData(MutableLiveData<User> liveData, String userId) {
        DocumentReference userDocRef = userCollection.document(userId);

        return userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "setUserLiveData: Listen failed.", e);
                liveData.setValue(null);
                return;
            }
            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Log.e(TAG, "setUserLiveData: document does not exist for ID: " + userId);
                liveData.setValue(null);
                return;
            }
            if (documentSnapshot != null) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    Log.d(TAG, "setUserLiveData: success for user with ID: " + userId);
                } else {
                    Log.e(TAG, "setUserLiveData: user is null after deserialization");
                }
                liveData.setValue(user);
            }
        });
    }

    /**
     * Updates the FCM (Firebase Cloud Messaging) token for the current user in Firestore.
     *
     * @param token The new FCM token to associate with the user.
     * @return A CompletableFuture indicating the success or failure of the operation.
     */
    public CompletableFuture<Void> updateUserFcmToken(String token) {
        User currentUser = currentUserLiveData.getValue();
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (currentUser == null || currentUser.getUserId() == null) {
            future.completeExceptionally(new IllegalStateException("Current user is null"));
            return future;
        }

        DocumentReference userRef = userCollection.document(currentUser.getUserId());
        userRef.update("fcmToken", token)
                .addOnSuccessListener(discard -> {
                    Log.d(TAG, "FCM token updated successfully");
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update FCM token", e);
                    future.completeExceptionally(e);
                });
        return future;
    }

    /**
     * Retrieves a LiveData list of all users.
     *
     * @return LiveData containing a list of all users.
     */
    public LiveData<List<User>> getAllUsersLiveData() {
        return Common.runQueryLiveData("getAllUsersLiveData", userCollection, User.class, TAG);
    }

    /**
     * Retrieves the LiveData for a specific user by userId.
     *
     * @param userId The ID of the user to retrieve.
     * @return LiveData containing the user data.
     */
    public LiveData<User> getUserLiveData(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        setUserLiveData(userLiveData, userId);
        return userLiveData;
    }

    /**
     * Retrieves a LiveData list of users by their IDs.
     *
     * @param userIds The list of user IDs to retrieve.
     * @return LiveData containing a list of users matching the given IDs.
     */
    public LiveData<List<User>> getUsersByIdsLiveData(List<String> userIds) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

        if (userIds == null || userIds.isEmpty()) {
            Log.d(TAG, "getUsersByIdsLiveData: No user IDs provided");
            usersLiveData.setValue(new ArrayList<>());
            return usersLiveData;
        }

        userCollection.whereIn(FieldPath.documentId(), userIds).addSnapshotListener((userQuerySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "getUsersByIdsLiveData: Failed to listen for users", e);
                usersLiveData.setValue(new ArrayList<>());
                return;
            }

            if (userQuerySnapshot != null && !userQuerySnapshot.isEmpty()) {
                List<User> users = userQuerySnapshot.toObjects(User.class);
                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setUserId(userQuerySnapshot.getDocuments().get(i).getId());
                }
                usersLiveData.setValue(users);
                Log.d(TAG, "getUsersByIdsLiveData: Retrieved " + users.size() + " users");
            } else {
                Log.d(TAG, "getUsersByIdsLiveData: No users found");
                usersLiveData.setValue(new ArrayList<>());
            }
        });
        return usersLiveData;
    }
}
