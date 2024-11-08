package com.example.eventapp.ui.events;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ScannedEventFragment extends BottomSheetDialogFragment {

    private EventsViewModel eventsViewModel;
    private final Event event;
    private int currentWaitlistButtonState;

    public ScannedEventFragment (Event event) {
        this.event = event;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        View view = inflater.inflate(R.layout.scanned_event_popup, null);
        TextView eventName = view.findViewById(R.id.popup_scanned_event_name_text);
        Button waitlistButton = view.findViewById(R.id.popup_scanned_event_waitlist_button);
        ImageView eventImage = view.findViewById(R.id.popup_scanned_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_scanned_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_scanned_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_scanned_event_description_text);
        TextView eventEntrantsCount = view.findViewById(R.id.popup_scanned_event_max_entrants);

        // Set views
        eventName.setText(event.getEventName());
        eventDuration.setText("From: " + FormatDate.format(event.getStartDate()) + " To: " + FormatDate.format(event.getEndDate()));
        eventRegistrationDeadline.setText("Registration Deadline: " + FormatDate.format(event.getDeadline()));
        eventDescription.setText(event.getEventDescription());

        if (event.hasPoster()) {
            Glide.with(this)
                    .load(event.getPosterUri())
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable.default_event_poster);
        }
        if (event.getMaxEntrants() != -1) {
            eventEntrantsCount.setText("Entrants: 0/" + event.getMaxEntrants());
        } else {
            eventEntrantsCount.setText("No Entrant Limit");
        }

        boolean isAlreadyInWaitlist = isAlreadyOnWaitlist(event);

        // initial button text
        if(isAlreadyInWaitlist){
            currentWaitlistButtonState = 1;
            waitlistButton.setText("Leave Waitlist");
        }else{
            currentWaitlistButtonState = 0;
            waitlistButton.setText("Join Waitlist");
        }

        waitlistButton.setOnClickListener(view1 -> {
            if(currentWaitlistButtonState == 1){ // leave waitlist
                leaveEventWaitlist(event);
                waitlistButton.setText("Join Waitlist");
                currentWaitlistButtonState = 0;
            }else{ // join waitlist
                joinEventWaitlist(event);
                waitlistButton.setText("Leave Waitlist");
                currentWaitlistButtonState = 1;
            }
        });

        return view;
    }

    private void joinEventWaitlist(Event event){
        eventsViewModel.registerToEvent(event);
    }

    private void leaveEventWaitlist(Event event){
        eventsViewModel.unregisterFromEvent(event);
    }

    private boolean isAlreadyOnWaitlist(Event event){
        return eventsViewModel.isSignedUp(event);
    }
}