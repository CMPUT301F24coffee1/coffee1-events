package com.example.eventapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Signup;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SignupRepository {

    private static final String TAG = "SignupRepository";
    private static SignupRepository instance;
    private final CollectionReference signupCollection;

    private SignupRepository() {
        signupCollection = FirebaseFirestore.getInstance().collection("signups");
    }

    private SignupRepository(FirebaseFirestore testInstance) {
        signupCollection = testInstance.collection("signups");
    }

    public static synchronized SignupRepository getInstance() {
        if (instance == null) {
            instance = new SignupRepository();
        }
        return instance;
    }

    public static synchronized SignupRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new SignupRepository(testInstance);
        }
        return instance;
    }

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

    public LiveData<List<Signup>> getSignupsOfUserLiveData(String userId) {
        Query query = signupCollection.whereEqualTo("userId", userId);
        return Common.runQueryLiveData("getSignupsOfUserLiveData", query, Signup.class, TAG);
    }

    public LiveData<List<Signup>> getSignupsOfEventLiveData(String eventId) {
        Query query = signupCollection.whereEqualTo("eventId", eventId);
        return Common.runQueryLiveData("getSignupsOfEventLiveData", query, Signup.class, TAG);
    }
}
