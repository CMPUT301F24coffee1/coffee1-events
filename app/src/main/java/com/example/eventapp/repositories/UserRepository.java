package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Admin;
import com.example.eventapp.models.Entrant;
import com.example.eventapp.models.Organizer;
import com.example.eventapp.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;


public class UserRepository {

    private static final String TAG = "UserRepository";
    private final CollectionReference userCollection;

    public UserRepository() {
        userCollection = FirebaseFirestore.getInstance().collection("users");
    }

    public static Class<? extends User> getUserClass(String userType) throws
            IllegalArgumentException {
        switch(userType) {
            case "Entrant":
                return Entrant.class;
            case "Organizer":
                return Organizer.class;
            case "Admin":
                return Admin.class;
            default:
                throw new IllegalArgumentException("Unknown user type: " + userType);
        }
    }

    public Task<Void> saveUser(User user) {
        String userId = user.getUserId();
        String userType = user.getUserType();
        if (userId == null) throw new NullPointerException("userId cannot be null");
        if (userType == null) throw new NullPointerException("userType cannot be null");

        return userCollection.document(userId).set(user)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "saveUser: success for user with ID: " + userId);
                } else {
                    Log.e(TAG, "saveUser: fail", task.getException());
                }
            });
    }

    public Task<Void> removeUser(String userId) {
        return userCollection.document(userId).delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "removeUser: success for user with ID: " + userId);
                } else {
                    Log.e(TAG, "removeUser: fail", task.getException());
                }
            });
    }

    public CompletableFuture<User> getUser(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();

        userCollection.document(userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String userType = documentSnapshot.getString("userType");

                    if (userType == null) {
                        Exception e = new NullPointerException("userType is null in document with ID: " + documentSnapshot.getId());
                        future.completeExceptionally(e);
                        Log.e(TAG, "getUser: fail", e);
                        return;
                    }
                    try {
                        Class<? extends User> userClass = UserRepository.getUserClass(userType);
                        User user = documentSnapshot.toObject(userClass);

                        future.complete(user);
                        Log.d(TAG, "getUser: success for user with ID: " + userId);
                    } catch (IllegalArgumentException e) {
                        future.completeExceptionally(e);
                        Log.e(TAG, "getUser: fail", e);
                    }
                }
                else if (task.isSuccessful() && !task.getResult().exists()) {
                    future.complete(null);
                    Log.d(TAG, "getUser: user does not exist for ID: " + userId);
                }
                else {
                    future.completeExceptionally(task.getException());
                    Log.e(TAG, "getUser: fail", task.getException());
                }
            });
        return future;
    }

    public LiveData<User> getUserLiveData(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        DocumentReference userDocRef = userCollection.document(userId);

        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "getUserLiveData: Listen failed.", e);
                liveData.setValue(null);
                return;
            }
            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Log.e(TAG, "getUserLiveData: document does not exist for ID: " + userId);
                liveData.setValue(null);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String userType = documentSnapshot.getString("userType");
                if (userType == null) {
                    Log.e(TAG, "getUserLiveData: userType is null for document ID: " + documentSnapshot.getId());
                    liveData.setValue(null);
                    return;
                }

                Class<? extends User> userClass;
                try {
                    userClass = UserRepository.getUserClass(userType);
                } catch (IllegalArgumentException ex) {
                    Log.e(TAG, "getUserLiveData: fail", ex);
                    liveData.setValue(null);
                    return;
                }
                User user = documentSnapshot.toObject(userClass);

                if (user != null) {
                    Log.d(TAG, "getUserLiveData: success for user with ID: " + userId);
                } else {
                    Log.e(TAG, "getUserLiveData: user is null after deserialization");
                }
                liveData.setValue(user);
            }
        });
        return liveData;
    }
}
