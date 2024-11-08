package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class UserRepository {

    private static final String TAG = "UserRepository";
    private static UserRepository instance;
    private final CollectionReference userCollection;

    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private String currentUserId = null;
    private ListenerRegistration currentListenerRegistration = null;

    private UserRepository() {
        userCollection = FirebaseFirestore.getInstance().collection("users");
    }

    private UserRepository(FirebaseFirestore testInstance) {
        userCollection = testInstance.collection("users");
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public static synchronized UserRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new UserRepository(testInstance);
        }
        return instance;
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

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

    public CompletableFuture<Void> removeUser(User user) {
        String userId = getUserIdOrThrow(user);
        return removeUser(userId);
    }

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

    private String getUserIdOrThrow(User user) throws NullPointerException {
        if (user == null) throw new NullPointerException("user cannot be null");
        String userId = user.getUserId();
        if (userId == null) throw new NullPointerException("userId cannot be null - set deviceId");
        return userId;
    }

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

    public LiveData<List<User>> getAllUsersLiveData() {
        return runQueryLiveData("getAllUsersLiveData", userCollection);
    }

    public LiveData<User> getUserLiveData(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        setUserLiveData(userLiveData, userId);
        return userLiveData;
    }

    private LiveData<List<User>> runQueryLiveData(String methodName, Query query) {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "runQueryLiveData: " + methodName + ": listen failed", e);
                liveData.setValue(new ArrayList<>());
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<User> users = querySnapshot.toObjects(User.class);

                Log.d(TAG, "runQueryLiveData: " + methodName + ": success");
                liveData.setValue(users);
            } else {
                Log.d(TAG, "runQueryLiveData: " + methodName + ": no documents found");
                liveData.setValue(new ArrayList<>());
            }
        });
        return liveData;
    }
}
