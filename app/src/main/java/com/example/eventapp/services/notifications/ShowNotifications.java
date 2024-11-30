package com.example.eventapp.services.notifications;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Notification;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.viewmodels.NotificationsViewModel;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ShowNotifications {

    public static void showInAppNotifications(Context context, QuerySnapshot notificationsSnapshot, NotificationsViewModel viewModel) {
        if (notificationsSnapshot.isEmpty()) {
            return; // No notifications to display
        }

        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : notificationsSnapshot) {
            Notification notification = doc.toObject(Notification.class);
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
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true
                    );

                    // Remove the default background
                    popupWindow.setBackgroundDrawable(null);

                    // Show the popup at the center of the anchor view
                    popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                    // Handle Accept Button click
                    btnAccept.setOnClickListener(v -> {
                        // Handle the action for accepting the event
                        // (You can add your own logic here)

                        // Close the popup
                        popupWindow.dismiss();
                    });

                    // Handle Decline Button click
                    btnDecline.setOnClickListener(v -> {
                        // Handle the action for declining the event
                        // (You can add your own logic here)

                        // Close the popup
                        popupWindow.dismiss();
                    });
                }
            });
        }
    }
}
