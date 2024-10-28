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
import com.google.firebase.firestore.FirebaseFirestore;


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

    public Task<Void> updateUser(User user) {
        return userCollection.document(user.getUserId()).set(user)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "updateUser: success for user with ID: " + user.getUserId());
                } else {
                    Log.e(TAG, "updateUser: fail", task.getException());
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

    public LiveData<User> getUserLiveData(String userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        DocumentReference userDocRef = userCollection.document(userId);

        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "getUser: Listen failed.", e);
                liveData.setValue(null);
                return;
            }
            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Log.e(TAG, "getUser: document does not exist for ID: " + userId);
                liveData.setValue(null);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String userType = documentSnapshot.getString("userType");
                if (userType == null) {
                    Log.e(TAG, "getUser: userType is null for document ID: " + documentSnapshot.getId());
                    liveData.setValue(null);
                    return;
                }

                Class<? extends User> userClass;
                try {
                    userClass = UserRepository.getUserClass(userType);
                } catch (IllegalArgumentException ex) {
                    Log.e(TAG, "getUser: fail", ex);
                    liveData.setValue(null);
                    return;
                }
                User user = documentSnapshot.toObject(userClass);

                if (user != null) {
                    Log.d(TAG, "getUser: success for user with ID: " + userId);
                } else {
                    Log.e(TAG, "getUser: user is null after deserialization");
                }
                liveData.setValue(user);
            }
        });
        return liveData;
    }
}
