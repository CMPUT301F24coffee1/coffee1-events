package com.example.eventapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eventapp.models.Notification;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.services.NotificationService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The NotificationDialogViewModel class handles business logic for the notification dialog.
 * It facilitates retrieving event data and updating signup statuses and notifications.
 */
public class NotificationDialogViewModel extends ViewModel {

    private static final String TAG = "NotificationDialogViewModel";

    private final NotificationService notificationService;
    private final SignupRepository signupRepository;

    /**
     * Default constructor for NotificationDialogViewModel.
     * Initializes the view model with default repositories and services.
     */
    public NotificationDialogViewModel() {
        this(
                NotificationService.getInstance(),
                SignupRepository.getInstance()
        );
    }

    /**
     * Parameterized constructor for NotificationDialogViewModel for dependency injection.
     *
     * @param notificationService The NotificationService instance.
     * @param signupRepository The SignupRepository instance.
     */
    public NotificationDialogViewModel(
            NotificationService notificationService,
            SignupRepository signupRepository) {
        this.notificationService = notificationService;
        this.signupRepository = signupRepository;
    }

    /**
     * Updates the signup status for a given user and event based on the user's decision.
     *
     * @param notification          The notification containing user and event information.
     * @param userAcceptedInvitation Whether the user accepted the invitation.
     * @return A CompletableFuture indicating the success or failure of the operation.
     */
    public CompletableFuture<Void> updateSignupStatus(Notification notification, boolean userAcceptedInvitation) {
        String userId = notification.getUserId();
        String eventId = notification.getEventId();

        if (userId == null || eventId == null) {
            Log.e(TAG, "updateSignupStatus: Missing userId or eventId in notification.");
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalArgumentException("Notification must contain valid userId and eventId."));
            return failedFuture;
        }

        return signupRepository.getSignup(userId, eventId)
            .thenCompose(signup -> {
                if (signup == null) {
                    Log.e(TAG, "updateSignupStatus: No signup found for userId: " + userId + ", eventId: " + eventId);
                    CompletableFuture<Void> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new IllegalStateException("Signup not found."));
                    return failedFuture;
                }

                signup.setEnrolled(userAcceptedInvitation);
                signup.setCancelled(!userAcceptedInvitation);
                return signupRepository.updateSignup(signup);
            })
            .thenRun(() -> Log.i(TAG, "updateSignupStatus: Successfully updated signup for userId: " + userId + ", eventId: " + eventId))
            .exceptionally(throwable -> {
                Log.e(TAG, "updateSignupStatus: Failed to update signup status.", throwable);
                throw new CompletionException(throwable);
            });
    }

    /**
     * Deletes a notification from the notification service.
     *
     * @param notification The notification to delete.
     * @return A CompletableFuture indicating the success or failure of the deletion operation.
     */
    public CompletableFuture<Void> deleteNotification(Notification notification) {
        return notificationService.deleteNotification(notification);
    }
}
