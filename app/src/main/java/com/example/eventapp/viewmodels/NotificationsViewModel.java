package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Notification;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.NotificationRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NotificationsViewModel {

    private final String TAG = "EventsViewModel";
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;

    /**
     * Default constructor for NotificationsViewModel.
     */
    public NotificationsViewModel() {
        this(   NotificationRepository.getInstance(),
                EventRepository.getInstance());
    }

    /**
     * Constructor used for DI in tests.
     */
    public NotificationsViewModel(NotificationRepository notificationRepository, EventRepository eventRepository) {
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves an event from the events repository based on its Event ID.
     *
     * @param eventId The hash associated with the event's QR code.
     * @return A CompletableFuture containing the event associated with the QR code.
     */
    public CompletableFuture<Event> getEventById(String eventId){
        return eventRepository.getEventById(eventId);
    }

    /**
     * Adds a new notification to the correct users collection.
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
        CompletableFuture<Void> deleteNotificationFuture = notificationRepository.deleteNotification(notification.getUserId(), notification.getDocumentId());

        deleteNotificationFuture.thenAccept(discard -> {
            Log.i(TAG, "Removed notification with name: " + notification.getTitle());
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to remove notification", throwable);
            return null;
        });
        return deleteNotificationFuture;
    }

    /**
     * Calls a Cloud Function to add the user to the correct list
     * The possible lists are accepted or declined
     *
     * @param eventId the event id for the notification
     * @param userAcceptedInvitation whether the user accepted the invitation to join the event
     */
    public void UpdateSignupStatus(String eventId, Boolean userAcceptedInvitation ) {
        // Calls Cloud Function to update the signup status
    }
}
