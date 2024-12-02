package com.example.eventapp.services;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.eventapp.models.Notification;
import com.example.eventapp.repositories.NotificationRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NotificationService {

    private static final String TAG = "NotificationService";
    private static NotificationService instance;
    private final NotificationRepository notificationRepository;

    /**
     * Default constructor for NotificationService.
     */
    private NotificationService() {
        this.notificationRepository = NotificationRepository.getInstance();
    }

    /**
     * Constructor used for DI in tests.
     */
    private NotificationService(FirebaseFirestore testInstance) {
        this.notificationRepository = NotificationRepository.getTestInstance(testInstance);
    }

    /**
     * Retrieves the singleton instance of NotificationService.
     *
     * @return The singleton instance of NotificationService.
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Retrieves a test instance of NotificationService using a specified Firestore instance.
     *
     * @param testInstance The Firestore test instance to use.
     * @return A singleton test instance of NotificationService.
     */
    public static synchronized NotificationService getTestInstance(FirebaseFirestore testInstance) {
        if (instance == null) {
            instance = new NotificationService(testInstance);
        }
        return instance;
    }

    /**
     * Fetches live data of unread notifications for a user.
     * @param userId The ID of the user to fetch notifications for.
     */
    public LiveData<List<Notification>> fetchNotificationsLiveData(String userId) {
        return notificationRepository.fetchNotificationsLiveData(userId);
    }

    /**
     * Adds a new notification to the correct users' collection.
     *
     * @param notification The notification to add.
     * @return A CompletableFuture containing the notification's document ID.
     */
    public CompletableFuture<String> uploadNotification(Notification notification) {
        CompletableFuture<String> future = new CompletableFuture<>();

        notificationRepository.uploadNotification(notification)
                .thenAccept(s -> Log.d(TAG, "Notification uploaded successfully!"))
                .exceptionally(throwable -> {
                    Log.e(TAG, "Failed to upload notification", throwable);
                    return null;
                });
        return future;
    }

    /**
     * Removes a notification from the database.
     *
     * @param notification The notification to remove.
     * @return A CompletableFuture indicating the completion of the removal.
     */
    public CompletableFuture<Void> deleteNotification(Notification notification) {
        CompletableFuture<Void> deleteNotificationFuture = notificationRepository
                .deleteNotification(notification.getUserId(), notification.getDocumentId());

        deleteNotificationFuture.thenAccept(discard -> {
            Log.i(TAG, "Removed notification with name: " + notification.getTitle());
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to remove notification", throwable);
            return null;
        });
        return deleteNotificationFuture;
    }
}
