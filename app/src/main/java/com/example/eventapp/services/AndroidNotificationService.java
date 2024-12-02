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

public class AndroidNotificationService extends FirebaseMessagingService {

    private static final String TAG = "AndroidNotificationService";
    private static final String CHANNEL_ID = "event_notifications_channel";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        sendRegistrationTokenToServer(token);
    }

    private void sendRegistrationTokenToServer(String token) {
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.updateUserFcmToken(token)
                .thenAccept(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                .exceptionally(e -> {
                    Log.e(TAG, "Failed to save FCM token", e);
                    return null;
                });
    }

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
