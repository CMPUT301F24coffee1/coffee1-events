package com.example.eventapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.eventapp.models.Signup;
import com.example.eventapp.models.User;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The `SignupRepository` class is a singleton repository that provides access to Firestore operations
 * for managing `Signup` data. It supports adding, updating, retrieving, and removing signup records,
 * along with providing LiveData-based methods for observing lists of signups filtered by user ID
 * or event ID. This class enables seamless interaction with Firestore for sign-up data management
 * within the application.
 */
public class SignupRepository {

    private static final String TAG = "SignupRepository";
    private static SignupRepository instance;
    private final CollectionReference signupCollection;
    private final UserRepository userRepository;

    private SignupRepository() {
        signupCollection = FirebaseFirestore.getInstance().collection("signups");
        userRepository = UserRepository.getInstance();
    }

    private SignupRepository(FirebaseFirestore testInstance) {
        signupCollection = testInstance.collection("signups");
        userRepository = UserRepository.getTestInstance(testInstance);
    }

    /**
     * Retrieves a singleton instance of SignupRepository.
     *
     * @return The singleton instance of SignupRepository.
     */
    public static synchronized SignupRepository getInstance() {
        if (instance == null) {
            instance = new SignupRepository();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of SignupRepository with the specified Firestore instance.
     *
     * @param testInstance The Firestore instance for testing.
     * @return A test instance of SignupRepository.
     */
    public static synchronized SignupRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new SignupRepository(testInstance);
        }
        return instance;
    }

    /**
     * Adds a signup document to Firestore.
     *
     * @param signup The signup details to be added.
     * @return A CompletableFuture containing the document ID of the added signup.
     * @throws NullPointerException if signup, userId, or eventId is null.
     */
    public CompletableFuture<String> addSignup(Signup signup) {
        Objects.requireNonNull(signup);
        String userId = signup.getUserId();
        String eventId = signup.getEventId();

        CompletableFuture<String> future = new CompletableFuture<>();

        if (userId == null) {
            future.completeExceptionally(new NullPointerException("userId cannot be null"));
            return future;
        }
        if (eventId == null) {
            future.completeExceptionally(new NullPointerException("eventId cannot be null"));
            return future;
        }
        signup.setSignupTimestamp(System.currentTimeMillis());

        signupCollection.add(signup)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String documentId = task.getResult().getId();
                        signup.setDocumentId(documentId);
                        Log.d(TAG, "addSignup: success - ID: " + documentId);
                        future.complete(documentId);
                    } else {
                        Log.e(TAG, "addSignup: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Updates an existing signup document in Firestore.
     *
     * @param signup The signup document with updated information.
     * @return A CompletableFuture indicating the completion of the update.
     * @throws NullPointerException if signup or documentId is null.
     */
    public CompletableFuture<Void> updateSignup(Signup signup) {
        Objects.requireNonNull(signup);
        String documentId = signup.getDocumentId();

        CompletableFuture<Void> future = new CompletableFuture<>();

        if (documentId == null) {
            future.completeExceptionally(new NullPointerException("documentId is null - never set documentId"));
            return future;
        }

        signupCollection.document(documentId).set(signup)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateSignup: success - ID: " + documentId);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "updateSignup: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    /**
     * Removes a signup document from Firestore based on userId and eventId.
     *
     * @param userId The user ID associated with the signup.
     * @param eventId The event ID associated with the signup.
     * @return A CompletableFuture indicating the completion of the removal.
     */
    public CompletableFuture<Void> removeSignup(String userId, String eventId) {

        return getSignup(userId, eventId)
            .thenCompose(signup -> {
                if (signup != null) {
                    return removeSignup(signup);
                } else {
                    Log.d(TAG, "removeSignup: no signup found for userId: " + userId + ", eventId: " + eventId);
                    return CompletableFuture.completedFuture(null);
                }
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "removeSignup: failed to remove signup", throwable);
                return null;
            });
    }

    /**
     * Removes a specified signup document from Firestore.
     *
     * @param signup The signup document to be removed.
     * @return A CompletableFuture indicating the completion of the removal.
     * @throws NullPointerException if signup or documentId is null.
     */
    public CompletableFuture<Void> removeSignup(Signup signup) {
        Objects.requireNonNull(signup);
        String documentId = signup.getDocumentId();

        CompletableFuture<Void> future = new CompletableFuture<>();

        if (documentId == null) {
            Log.e(TAG, "removeSignup: documentId is null");
            future.completeExceptionally(new NullPointerException("documentId is null"));
            return future;
        }

        signupCollection.document(documentId).delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "removeSignup: success - ID: " + documentId);
                    future.complete(null);
                } else {
                    Log.e(TAG, "removeSignup: fail", task.getException());
                    future.completeExceptionally(task.getException());
                }
            });
        return future;
    }

    /**
     * Retrieves a signup document based on userId and eventId.
     *
     * @param userId The user ID associated with the signup.
     * @param eventId The event ID associated with the signup.
     * @return A CompletableFuture containing the signup, or null if not found.
     */
    public CompletableFuture<Signup> getSignup(String userId, String eventId) {
        CompletableFuture<Signup> future = new CompletableFuture<>();

        signupCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    Signup signup = querySnapshot.toObjects(Signup.class).get(0);

                    if (signup != null) {
                        Log.d(TAG, "getSignup: success for userId: " + userId + " eventId: " + eventId);
                        signup.setDocumentId(querySnapshot.getDocuments().get(0).getId());
                    } else {
                        Log.e(TAG, "getSignup: signup is null after deserialization");
                    }
                    future.complete(signup);
                }
                else if (task.isSuccessful() && task.getResult().isEmpty()) {
                    Log.d(TAG, "getSignup: signup does not exist for userId: " + userId + " eventId: " + eventId);
                    future.complete(null);
                }
                else {
                    Log.e(TAG, "getSignup: fail", task.getException());
                    future.completeExceptionally(task.getException());
                }
            });
        return future;
    }

    /**
     * Retrieves all users signed up for a specific event.
     *
     * @param eventId The ID of the event.
     * @return LiveData containing a list of User instances signed up for the event.
     */
    public LiveData<List<User>> getSignedUpUsersLiveData(String eventId) {
        return getSignedUpUsersByFilterLiveData(eventId, new SignupFilter());
    }

    /**
     * Retrieves all users signed up for a specific event, filtered by a SignupFilter.
     *
     * @param eventId The ID of the event.
     * @param filter instance of SignupFilter, outlining what to filter Signups by.
     * @return LiveData containing a list of User instances signed up for the event.
     */
    public LiveData<List<User>> getSignedUpUsersByFilterLiveData(String eventId, SignupFilter filter) {
        Query query = signupCollection.whereEqualTo("eventId", eventId);

        if (filter.isCancelled != null) {
            query = query.whereEqualTo("isCancelled", filter.isCancelled);
        }
        if (filter.isWaitlisted != null) {
            query = query.whereEqualTo("isWaitlisted", filter.isWaitlisted);
        }
        if (filter.isChosen != null) {
            query = query.whereEqualTo("isChosen", filter.isChosen);
        }
        if (filter.isEnrolled != null) {
            query = query.whereEqualTo("isEnrolled", filter.isEnrolled);
        }

        LiveData<List<Signup>> signupLiveData = Common.runQueryLiveData(
                "getSignedUpUsersByFilter", query, Signup.class, TAG);

        return Transformations.switchMap(signupLiveData, signups -> {
            if (signups == null || signups.isEmpty()) {
                MutableLiveData<List<User>> emptyLiveData = new MutableLiveData<>();
                emptyLiveData.setValue(new ArrayList<>());
                return emptyLiveData;
            }

            List<String> userIds = new ArrayList<>();
            for (Signup signup : signups) {
                userIds.add(signup.getUserId());
            }

            return userRepository.getUsersByIdsLiveData(userIds);
        });
    }

    /**
     * Retrieves a LiveData list of signups associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return LiveData containing a list of signups for the specified user.
     */
    public LiveData<List<Signup>> getSignupsOfUserLiveData(String userId) {
        Query query = signupCollection.whereEqualTo("userId", userId);
        return Common.runQueryLiveData("getSignupsOfUserLiveData", query, Signup.class, TAG);
    }

    /**
     * Retrieves a LiveData list of signups associated with a specific event.
     *
     * @param eventId The ID of the event.
     * @return LiveData containing a list of signups for the specified event.
     */
    public LiveData<List<Signup>> getSignupsOfEventLiveData(String eventId) {
        Query query = signupCollection.whereEqualTo("eventId", eventId);
        return Common.runQueryLiveData("getSignupsOfEventLiveData", query, Signup.class, TAG);
    }
}
