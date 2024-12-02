package com.example.eventapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.eventapp.R;
import com.example.eventapp.repositories.UserRepository;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service to handle Firebase Cloud Messaging (FCM) notifications.
 * This class is responsible for receiving, processing, and displaying notifications sent via FCM.
 */
public class AndroidNotificationService extends FirebaseMessagingService {

    private static final String TAG = "AndroidNotificationService";
    private static final String CHANNEL_ID = "event_notifications_channel";

    /**
     * Called when a new FCM token is generated for the device.
     * This token can be used to uniquely identify the device for sending notifications.
     *
     * @param token The new FCM token.
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        sendRegistrationTokenToServer(token);
    }

    /**
     * Sends the FCM token to the server to associate it with the current user.
     *
     * @param token The FCM token to send.
     */
    private void sendRegistrationTokenToServer(String token) {
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.updateUserFcmToken(token)
                .thenAccept(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                .exceptionally(e -> {
                    Log.e(TAG, "Failed to save FCM token", e);
                    return null;
                });
    }

    /**
     * Called when a message is received from FCM.
     * This method handles both notification payloads and data payloads.
     *
     * @param remoteMessage The message received from FCM.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }

        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message Data Payload: " + remoteMessage.getData());
        }
    }

    /**
     * Displays a notification in the system notification tray.
     * Handles creating a notification channel on Android 8.0+.
     *
     * @param title   The title of the notification.
     * @param message The message body of the notification.
     */
    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create the NotificationChannel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Event Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
