package com.example.eventapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Signup;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SignupRepository {

    private static final String TAG = "SignupRepository";
    private static SignupRepository instance;
    private final CollectionReference signupCollection;

    private SignupRepository() {
        signupCollection = FirebaseFirestore.getInstance().collection("signups");
    }

    public static synchronized SignupRepository getInstance() {
        if (instance == null) {
            instance = new SignupRepository();
        }
        return instance;
    }

    public Task<DocumentReference> addSignup(Signup signup) {
        String userId = signup.getUserId();
        String eventId = signup.getEventId();
        if (userId == null) throw new NullPointerException("userId cannot be null");
        if (eventId == null) throw new NullPointerException("eventId cannot be null");

        return signupCollection.add(signup)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "addSignup: success - ID: " + task.getResult().getId());
                } else {
                    Log.e(TAG, "addSignup: fail", task.getException());
                }
            });
    }

    public Task<Void> updateSignup(Signup signup) {
        String documentId = signup.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return signupCollection.document(documentId).set(signup)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "updateSignup: success - ID: " + documentId);
                } else {
                    Log.e(TAG, "updateSignup: fail", task.getException());
                }
            });
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        String documentId = signup.getDocumentId();

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
        return runQueryLiveData("getSignupsOfUserLiveData", query);
    }

    public LiveData<List<Signup>> getSignupsOfEventLiveData(String eventId) {
        Query query = signupCollection.whereEqualTo("eventId", eventId);
        return runQueryLiveData("getSignupsOfEventLiveData", query);
    }

    private LiveData<List<Signup>> runQueryLiveData(String methodName, Query query) {
        MutableLiveData<List<Signup>> liveData = new MutableLiveData<>();

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "runQueryLiveData: " + methodName + ": listen failed", e);
                liveData.setValue(new ArrayList<>());
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Signup> signups = querySnapshot.toObjects(Signup.class);

                for (int i = 0; i < signups.size(); i++) {
                    signups.get(i).setDocumentId(querySnapshot.getDocuments().get(i).getId());
                }
                Log.d(TAG, "runQueryLiveData: " + methodName + ": success");
                liveData.setValue(signups);
            } else {
                Log.d(TAG, "runQueryLiveData: " + methodName + ": no documents found");
                liveData.setValue(new ArrayList<>());
            }
        });
        return liveData;
    }
}
