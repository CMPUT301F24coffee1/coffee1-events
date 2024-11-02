package com.example.eventapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Signup;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

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

    public Task<Void> removeSignup(Signup signup) {
        String documentId = signup.getDocumentId();
        if (documentId == null) throw new NullPointerException("documentId is null - never set documentId");

        return signupCollection.document(documentId).delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "removeSignup: success - ID: " + documentId);
                } else {
                    Log.e(TAG, "removeSignup: fail", task.getException());
                }
            });
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
