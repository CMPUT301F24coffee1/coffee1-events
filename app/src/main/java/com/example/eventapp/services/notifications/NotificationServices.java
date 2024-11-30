package com.example.eventapp.services.notifications;

import android.content.Context;
import android.util.Log;

import com.example.eventapp.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationServices {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "NotificationService";

    /**
     * Interface for failure callbacks.
     */
    public interface OnFailureCallback {
        void onFailure(Exception e);
    }

    /**
     * Interface for success callbacks when fetching notifications.
     */
    public interface OnFetchNotificationsCallback {
        void onSuccess(QuerySnapshot notifications);
        void onFailure(Exception e);
    }

    /**
     * Uploads a notification to a user's notifications subcollection.
     * @param userId The ID of the user to whom the notification is being sent.
     * @param notification The Notification object to be uploaded.
     * @param onSuccess Callback triggered upon successful upload.
     * @param onFailure Callback triggered upon failure with an exception.
     */
    public void uploadNotification(
            String userId,
            Notification notification,
            Runnable onSuccess,
            OnFailureCallback onFailure
    ) {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification uploaded successfully: " + documentReference.getId());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload notification", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Deletes a specific notification from a user's notifications subcollection.
     * @param userId The ID of the user from whose notifications the notification will be deleted.
     * @param notificationId The ID of the notification to be deleted.
     * @param onSuccess Callback triggered upon successful deletion.
     * @param onFailure Callback triggered upon failure with an exception.
     */
    public void deleteNotification(
            String userId,
            String notificationId,
            Runnable onSuccess,
            OnFailureCallback onFailure
    ) {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted successfully");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete notification", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Deletes all notifications for a user.
     * @param userId The ID of the user whose notifications will be deleted.
     * @param onSuccess Callback triggered upon successful deletion.
     * @param onFailure Callback triggered upon failure with an exception.
     */
    public void deleteAllNotifications(
            String userId,
            Runnable onSuccess,
            OnFailureCallback onFailure
    ) {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    db.runBatch(batch -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            batch.delete(document.getReference());
                        }
                    }).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "All notifications deleted successfully");
                        onSuccess.run();
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete all notifications", e);
                        onFailure.onFailure(e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve notifications", e);
                    onFailure.onFailure(e);
                });
    }

    /**
     * Fetches unread notifications for a user.
     * @param userId The ID of the user to fetch notifications for.
     * @param callback Callback to handle success or failure.
     */
    public void fetchUnreadNotifications(
            String userId,
            OnFetchNotificationsCallback callback
    ) {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch notifications", e);
                    callback.onFailure(e);
                });
    }
}

