package com.example.eventapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationServices {
    private static final String TAG = "NotificationHelper";
    private static final String NOTIFICATIONS_COLLECTION = "Notifications";
    private final FirebaseFirestore db;

    public NotificationServices() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Interface for notification callbacks
    public interface NotificationCallback {
        void onNotificationsFetched(List<Notification> notifications);

        void onError(Exception e);
    }

    // Send a notification to recipients
    public void sendNotification(Event event, String message, String type, List<String> recipients) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", event);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("recipients", recipients);
        notification.put("timestamp", FieldValue.serverTimestamp());

        db.collection(NOTIFICATIONS_COLLECTION).add(notification)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Notification sent with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error sending notification", e));
    }

    // Check for new notifications for a specific user
    public void checkNewNotifications(String userId, NotificationCallback callback) {
        db.collection(NOTIFICATIONS_COLLECTION)
                .whereArrayContains("recipients", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.setId(doc.getId()); // Save document ID for updates
                                notifications.add(notification);
                            }
                        }
                        callback.onNotificationsFetched(notifications);
                    } else {
                        Log.e(TAG, "Error checking notifications", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    // Display notifications
    public void displayNotifications(Context context, List<Notification> notifications) {
        for (Notification notification : notifications) {
            if ("invitation".equals(notification.getType())) {
                showEventInvitationPopup(context, notification);
            } else {
                showBasicNotificationPopup(context, notification);
            }
        }
    }

    // Show event invitation popup
    private void showEventInvitationPopup(Context context, Notification notification) {
        new AlertDialog.Builder(context)
                .setMessage(notification.getMessage())
                .setPositiveButton("Accept", (dialog, which) ->
                        handleNotificationResponse(notification, true))
                .setNegativeButton("Decline", (dialog, which) ->
                        handleNotificationResponse(notification, false))
                .show();
    }

    // Show basic notification popup
    private void showBasicNotificationPopup(Context context, Notification notification) {
        new AlertDialog.Builder(context)
                .setMessage(notification.getMessage())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Handle notification response
    private void handleNotificationResponse(Notification notification, boolean accepted) {
        if (notification.getId() == null) {
            Log.e(TAG, "Notification ID is null, cannot update response.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", accepted ? "Accepted" : "Declined");

        db.collection(NOTIFICATIONS_COLLECTION).document(notification.getId())
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Notification status updated"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating notification status", e));
    }
}
