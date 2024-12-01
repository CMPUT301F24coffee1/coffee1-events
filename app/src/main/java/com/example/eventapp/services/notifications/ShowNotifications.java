package com.example.eventapp.services.notifications;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Notification;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.services.FormatDate;

import java.util.List;

/**
 * Class for showing the current user's notifications.
 * Used whenever the user opens the app and has pending notifications.
 */
public class ShowNotifications {

    private static final String TAG = "ShowNotifications";
    private static NotificationService notificationService;
    private static EventRepository eventRepository;

    /**
     * Public method to display in-app notifications.
     * It handles different types of notifications by delegating to specific handler methods.
     *
     * @param context       The context from MainActivity.
     * @param notifications The list of notifications that need to be displayed.
     */
    public static void showInAppNotifications(Context context, List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return;
        }
        notificationService = NotificationService.getInstance();
        eventRepository = EventRepository.getInstance();

        for (Notification notification : notifications) {
            switch (notification.getType()) {
                case "Invitation":
                    handleInvitationNotification(context, notification);
                    break;
                case "General":
                    handleGeneralNotification(context, notification);
                    break;
                default:
                    Log.e(TAG, "showInAppNotifications: Invalid notification type. Must be 'General' or 'Invitation'.");
            }
        }
    }

    /**
     * Handles displaying invitation notifications.
     *
     * @param context             The context from MainActivity.
     * @param notification        The invitation notification to display.
     */
    private static void handleInvitationNotification(Context context, Notification notification) {
        eventRepository.getEventById(notification.getEvent()).thenAccept(event -> {
            if (event != null) {
                // Inflate custom layout
                View popupView = LayoutInflater.from(context).inflate(R.layout.event_invitation_popup, null);

                // Populate the UI elements
                TextView tvEventTitle = popupView.findViewById(R.id.tvEventTitle);
                TextView tvEventDate = popupView.findViewById(R.id.tvEventDate);
                TextView tvEventDescription = popupView.findViewById(R.id.tvEventDescription);
                ImageView ivEventImage = popupView.findViewById(R.id.ivEventImage);
                Button btnAccept = popupView.findViewById(R.id.btnAccept);
                Button btnDecline = popupView.findViewById(R.id.btnDecline);

                tvEventTitle.setText(event.getEventName());
                tvEventDate.setText(FormatDate.format(event.getStartDate()));
                tvEventDescription.setText(event.getEventDescription());

                if (event.hasPoster()) {
                    Glide.with(context).load(event.getPosterUri()).into(ivEventImage);
                } else {
                    ivEventImage.setImageResource(R.drawable.default_event_poster);
                }

                // Create the PopupWindow
                PopupWindow popupWindow = createPopupWindow(popupView);

                // Show the popup at the center of the screen
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                // Handle Accept Button click
                btnAccept.setOnClickListener(v -> {
                    // Handle the action for accepting the event
                    // TODO: UpdateSignupStatus!
                    // notificationService.updateSignupStatus(event.getDocumentId(), true);

                    // Delete the notification
                    notificationService.deleteNotification(notification);

                    // Close the popup
                    popupWindow.dismiss();
                });

                // Handle Decline Button click
                btnDecline.setOnClickListener(v -> {
                    // Handle the action for declining the event
                    // TODO: UpdateSignupStatus!
                    // notificationService.updateSignupStatus(event.getDocumentId(), false);

                    // Delete the notification
                    notificationService.deleteNotification(notification);

                    // Close the popup
                    popupWindow.dismiss();
                });
            } else {
                Log.w(TAG, "handleInvitationNotification: Event not found for ID " + notification.getEvent());
            }
        }).exceptionally(throwable -> {
            Log.e(TAG, "handleInvitationNotification: Failed to fetch event", throwable);
            return null;
        });
    }

    /**
     * Handles displaying general notifications.
     *
     * @param context             The context from MainActivity.
     * @param notification        The general notification to display.
     */
    private static void handleGeneralNotification(Context context, Notification notification) {
        // Inflate custom layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.general_notification_popup, null);

        // Populate the UI elements
        TextView tvMessageTitle = popupView.findViewById(R.id.tvMessageTitle);
        TextView tvMessageDescription = popupView.findViewById(R.id.tvMessageDescription);
        Button btnOkay = popupView.findViewById(R.id.btnOkay);

        tvMessageTitle.setText(notification.getTitle());
        tvMessageDescription.setText(notification.getMessage());

        // Show the popup at the center of the screen
        PopupWindow popupWindow = createPopupWindow(popupView);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        btnOkay.setOnClickListener(v -> {
            notificationService.deleteNotification(notification);
            popupWindow.dismiss();
        });
    }

    /**
     * Creates a configured PopupWindow for displaying notifications.
     *
     * @param popupView The view to display inside the popup.
     * @return A configured PopupWindow instance.
     */
    private static @NonNull PopupWindow createPopupWindow(View popupView) {
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        // Make the PopupWindow non-cancelable
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);

        // Remove the default background
        popupWindow.setBackgroundDrawable(null);
        return popupWindow;
    }
}