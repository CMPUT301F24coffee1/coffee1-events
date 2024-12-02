package com.example.eventapp.ui.notifications;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Notification;
import com.example.eventapp.models.Event;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.viewmodels.NotificationDialogViewModel;

import androidx.appcompat.app.AlertDialog;

/**
 * The NotificationDialogFragment class is responsible for displaying a dialog for notifications.
 * It dynamically handles invitation and general notification types with specific layouts and actions.
 */
public class NotificationDialogFragment extends DialogFragment {

    private static final String TAG = "NotificationDialogFragment";

    private static final String ARG_NOTIFICATION = "notification";
    private Notification notification;
    private static final String ARG_EVENT = "event";
    private Event event;

    private NotificationDialogViewModel viewModel;

    /**
     * Creates a new instance of the NotificationDialogFragment with the specified notification.
     *
     * @param notification The notification to be displayed in the dialog.
     * @param event The event to be displayed, if the notification is an invite.
     * @return A new instance of NotificationDialogFragment.
     */
    public static NotificationDialogFragment newInstance(Notification notification, @Nullable Event event) {
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTIFICATION, notification);
        if (event != null) {
            args.putSerializable(ARG_EVENT, event);
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment and retrieves the notification data from the arguments.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            notification = (Notification) getArguments().getSerializable(ARG_NOTIFICATION);
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        } else {
            Log.e(TAG, "Notification or Event data not found in bundle");
        }
        viewModel = new ViewModelProvider(this).get(NotificationDialogViewModel.class);
    }

    /**
     * Creates the dialog view and initializes the layout based on the notification type.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     * @return The created dialog instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view;
        switch (notification.getType()) {
            case "Invite":
                view = inflater.inflate(R.layout.event_invitation_popup, null);
                setupInvitationView(view);
                break;
            case "General":
                view = inflater.inflate(R.layout.general_notification_popup, null);
                setupGeneralView(view);
                break;
            default:
                Log.e(TAG, "Unknown notification type: " + notification.getType());
                return super.onCreateDialog(savedInstanceState);
        }
        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Make background transparent, for rounded corners.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    /**
     * Configures the view for invitation-type notifications and initializes UI elements.
     *
     * @param view The root view of the dialog layout.
     */
    private void setupInvitationView(View view) {
        TextView tvEventTitle = view.findViewById(R.id.tvEventTitle);
        TextView tvEventDate = view.findViewById(R.id.tvEventDate);
        TextView tvEventDescription = view.findViewById(R.id.tvEventDescription);
        ImageView ivEventImage = view.findViewById(R.id.ivEventImage);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);

        // Use event data
        if (event == null) {
            Log.e(TAG, "Event not found. Event must be in bundle for Notifications of type Invite.");
            dismiss();
            return;
        }
        tvEventTitle.setText(event.getEventName());
        tvEventDate.setText(FormatDate.format(event.getStartDate()));
        tvEventDescription.setText(event.getEventDescription());

        if (event.hasPoster()) {
            Glide.with(this).load(event.getPosterUri()).into(ivEventImage);
        } else {
            ivEventImage.setImageResource(R.drawable.default_event_poster);
        }

        btnAccept.setOnClickListener(v -> {
            viewModel.updateSignupStatus(notification, true)
                    .exceptionally(this::handleError);
            viewModel.deleteNotification(notification)
                    .exceptionally(this::handleError);
            dismiss();
        });

        btnDecline.setOnClickListener(v -> {
            viewModel.updateSignupStatus(notification, false)
                    .exceptionally(this::handleError);
            viewModel.deleteNotification(notification)
                    .exceptionally(this::handleError);
            dismiss();
        });
    }

    /**
     * Configures the view for general-type notifications and initializes UI elements.
     *
     * @param view The root view of the dialog layout.
     */
    private void setupGeneralView(View view) {
        TextView tvMessageTitle = view.findViewById(R.id.tvMessageTitle);
        TextView tvMessageDescription = view.findViewById(R.id.tvMessageDescription);
        Button btnOkay = view.findViewById(R.id.btnOkay);

        tvMessageTitle.setText(notification.getTitle());
        tvMessageDescription.setText(notification.getMessage());

        btnOkay.setOnClickListener(v -> {
            viewModel.deleteNotification(notification);
            dismiss();
        });
    }

    /**
     * Handles errors during asynchronous operations by displaying a toast message.
     *
     * @param throwable The exception that occurred.
     * @return Null to conform to CompletableFuture's exceptionally handler.
     */
    private Void handleError(Throwable throwable) {
        String errorMessage = throwable.getMessage() != null
                ? throwable.getMessage()
                : "An unknown error occurred";

        Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
        return null;
    }
}
