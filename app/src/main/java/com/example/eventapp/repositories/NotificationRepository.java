package com.example.eventapp.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.eventapp.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private static NotificationRepository instance;
    private final FirebaseFirestore db;

    /**
     * Initializes a new instance of NotificationRepository with the default Firebase instance.
     */
    private NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Initializes a new instance of NotificationRepository with a specified Firestore test instance.
     *
     * @param testInstance The Firestore instance to use, used in tests.
     */
    private NotificationRepository(FirebaseFirestore testInstance) {
        db = testInstance;
    }

    /**
     * Retrieves the singleton instance of NotificationRepository.
     *
     * @return The singleton instance of NotificationRepository.
     */
    public static synchronized NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of NotificationRepository using a specified Firestore instance.
     *
     * @param testInstance The Firestore test instance to use.
     * @return A singleton test instance of NotificationRepository.
     */
    public static synchronized NotificationRepository getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new NotificationRepository(testInstance);
        }
        return instance;
    }

    /**
     * Uploads a notification to a user's notifications subcollection.
     * @param notification The Notification object to be uploaded.
     */
    public CompletableFuture<String> uploadNotification(Notification notification) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.collection("users")
                .document(notification.getUserId())
                .collection("notifications")
                .add(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        future.complete(task.getResult().getId());
                    }
                    else {
                        Log.e(TAG, "uploadnotification: fail", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    /**
     * Deletes a specific notification from a user's notifications subcollection.
     * @param userId The ID of the user from whose notifications the notification will be deleted.
     * @param notificationId The ID of the notification to be deleted.
     */
    public CompletableFuture<Void> deleteNotification(String userId, String notificationId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Notification deleted successfully");
                        future.complete(null);
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    /**
     * Fetches unread notifications for a user as LiveData.
     *
     * @param userId The ID of the user to fetch notifications for.
     * @return LiveData containing a list of unread notifications.
     */
    public LiveData<List<Notification>> fetchNotificationsLiveData(String userId) {
        Query query = db.collection("users")
                .document(userId)
                .collection("notifications");

        return Common.runQueryLiveData("fetchUnreadNotificationsLiveData", query, Notification.class, TAG);
    }
}
