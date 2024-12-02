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

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Notification;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.viewmodels.NotificationsViewModel;

import java.util.List;
import java.util.Objects;

/**
 * Class for showing the current users notifications
 * Used whenever the user opens the app and has waiting notifications
 */
public class ShowNotifications {
    private static final String TAG = "ShowNotifications";

    /**
     * This is where the popups are made for each notification the user has.
     * Displays both invitation and general notifications.
     *
     * @param context get the context from Main Activity
     * @param notifications get the list of notifications that need to be displayed
     * @param viewModel the view model to display the notifications
     */
    public static void showInAppNotifications(Context context, List<Notification> notifications, NotificationsViewModel viewModel) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        for (Notification notification : notifications) {
            if (Objects.equals(notification.getType(), "Invitation")) {
                viewModel.getEventById(notification.getEvent()).thenAccept(event -> {
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
                        PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                true
                        );

                        // Remove the default background
                        popupWindow.setBackgroundDrawable(null);

                        // Show the popup at the center of the anchor view
                        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                        // Handle Accept Button click
                        btnAccept.setOnClickListener(v -> {
                            // Handle the action for accepting the event
                            viewModel.UpdateSignupStatus(event.getDocumentId(), true);

                            // Delete the notification
                            viewModel.deleteNotification(notification);

                            // Close the popup
                            popupWindow.dismiss();
                        });

                        // Handle Decline Button click
                        btnDecline.setOnClickListener(v -> {
                            // Handle the action for declining the event
                            viewModel.UpdateSignupStatus(event.getDocumentId(), false);

                            // Delete the notification
                            viewModel.deleteNotification(notification);

                            // Close the popup
                            popupWindow.dismiss();
                        });
                    }
                });
            } else if (Objects.equals(notification.getType(), "General")) {
                // Inflate custom layout
                View popupView = LayoutInflater.from(context).inflate(R.layout.general_notification_popup, null);

                // Populate the UI elements

                TextView tvMessageTitle = popupView.findViewById(R.id.tvMessageTitle);
                TextView tvMessageDescription = popupView.findViewById(R.id.tvMessageDescription);
                Button btnOkay = popupView.findViewById(R.id.btnOkay);

                tvMessageTitle.setText(notification.getTitle());
                tvMessageDescription.setText(notification.getMessage());

                // Create the PopupWindow
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

                // Show the popup at the center of the anchor view
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                // Handle Accept Button click
                btnOkay.setOnClickListener(v -> {
                    viewModel.deleteNotification(notification);
                    // Close the popup
                    popupWindow.dismiss();
                });
            } else {
                Log.w(TAG, "showNotification: the notification does not have a valid type, must be 'General' or 'Invitation'.");
            }
        }
    }
}
