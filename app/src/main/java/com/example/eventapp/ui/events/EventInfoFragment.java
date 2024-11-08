package com.example.eventapp.ui.events;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EventInfoFragment extends BottomSheetDialogFragment {

    private final Event event;
    private final EventsFragment eventsFragment;
    private int currentWaitlistButtonState;

    /**
     * This is the constructor for the Event Info Fragment.
     * It is used to get the necessary data to show event info and edit the event
     * @param event Event that is clicked and needs to display info for
     * @param eventsFragment The main events fragment instance which can be used to
     *                       call the show function for the edit event
     */
    public EventInfoFragment (Event event, EventsFragment eventsFragment) {
        this.event = event;
        this.eventsFragment = eventsFragment;
    }

    /**
     * Interface for main event fragment to implement in order to edit event
     */
    interface EditEventInfoListener{
        void editEventInfo(Event event);
    }

    interface waitlistListener{
        void joinEventWaitlist(Event event);
        void leaveEventWaitlist(Event event);
        boolean isAlreadyOnWaitlist(Event event);
    }

    /**
     * Initialize and run the event info fragment with the current event's info.
     * Includes the edit event button which initializes the edit event fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.event_info_popup, null);
        TextView eventName = view.findViewById(R.id.popup_event_name_text);
        Button editEventButton = view.findViewById(R.id.popup_edit_event_info_button);
        Button waitlistButton = view.findViewById(R.id.popup_event_waitlist_button);
        ImageView eventImage = view.findViewById(R.id.popup_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_event_description_text);
        TextView eventEntrantsCount = view.findViewById(R.id.popup_create_event_max_entrants);

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

        boolean isAlreadyInWaitlist = eventsFragment.isAlreadyOnWaitlist(event);

        // initial button text
        if(isAlreadyInWaitlist){
            currentWaitlistButtonState = 1;
            waitlistButton.setText("Leave Waitlist");
        }else{
            currentWaitlistButtonState = 0;
            waitlistButton.setText("Join Waitlist");
        }

        waitlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentWaitlistButtonState == 1){ // leave waitlist
                    eventsFragment.leaveEventWaitlist(event);
                    waitlistButton.setText("Join Waitlist");
                }else{
                    eventsFragment.joinEventWaitlist(event);
                    waitlistButton.setText("Leave Waitlist");
                }
            }
        });

        // Initializes the edit event fragment with the given event
        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsFragment.showEditEventPopup(event);
            }
        });
        return view;
    }
}
