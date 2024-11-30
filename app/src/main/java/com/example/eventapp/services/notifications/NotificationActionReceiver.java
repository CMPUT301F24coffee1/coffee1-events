package com.example.eventapp.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String notificationId = intent.getStringExtra("notificationId");
        String eventId = intent.getStringExtra("eventId");

        if ("ACCEPT_ACTION".equals(action)) {
            handleAcceptAction(notificationId, eventId);
        } else if ("DECLINE_ACTION".equals(action)) {
            handleDeclineAction(notificationId, eventId);
        }
    }

    private void handleAcceptAction(String notificationId, String eventId) {
        Log.d("NotificationAction", "Accept clicked for notification: " + notificationId);

        // Update Firestore to mark the user as accepted
        FirebaseFirestore.getInstance().collection("notifications")
                .document(notificationId)
                .update("recipients", FieldValue.arrayRemove(getCurrentUserId()));

        // Add logic to put the user as a participant in the event
    }

    private void handleDeclineAction(String notificationId, String eventId) {
        Log.d("NotificationAction", "Decline clicked for notification: " + notificationId);

        // Update Firestore to remove the user from recipients
        FirebaseFirestore.getInstance().collection("notifications")
                .document(notificationId)
                .update("recipients", FieldValue.arrayRemove(getCurrentUserId()));

        // Add logic to put the user on the cancelled list
    }

    private String getCurrentUserId() {
        // Replace with the actual logic to fetch the current user's ID
        return "current_user_id";
    }
}
