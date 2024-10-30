package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    public Task<Void> saveUser(User user) {
        String userId = user.getUserId();
        if (userId == null) throw new NullPointerException("userId cannot be null - set deviceId");

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
                User user = documentSnapshot.toObject(User.class);

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
