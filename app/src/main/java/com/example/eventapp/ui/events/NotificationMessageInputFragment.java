package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.viewmodels.EntrantsViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

public class NotificationMessageInputFragment extends DialogFragment {
    private NotificationMessageInputListener listener;
    interface NotificationMessageInputListener {
        void notifySelected(String messageContents);
    }

    public NotificationMessageInputFragment(NotificationMessageInputListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        EntrantsViewModel entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
        View view = getLayoutInflater().inflate(R.layout.fragment_notification_message_input, null);
        TextInputEditText messageContent = view.findViewById(R.id.fragment_notification_message_input_edittext_input);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder.setView(view).setTitle("Input Notification Message").setNegativeButton("Cancel", null)
                .setPositiveButton("Send", (dialogInterface, i) -> {
                    String notificationMessage = Objects.requireNonNull(messageContent.getText()).toString();

                    if(!notificationMessage.isEmpty()){
                        // send messages to users
                        listener.notifySelected(notificationMessage);
                    }
                }).create();
    }
}
