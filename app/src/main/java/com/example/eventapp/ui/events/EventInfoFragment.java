package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.Signup;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.ui.images.ImageInfoFragment;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * A fragment displaying detailed information about an event in a bottom sheet dialog.
 * Allows users to view event details, join or leave the event waitlist, and edit the event if they have permission.
 *
 * <p>Constructed with an {@link Event} object and an {@link EventsFragment} reference, enabling event editing functionality.
 * Uses {@link EventsViewModel} to manage waitlist actions and check edit permissions.</p>
 */
public class EventInfoFragment extends BottomSheetDialogFragment {

    private EventsViewModel eventsViewModel;
    private final Event event;
    private final EventsFragment eventsFragment;
    private int currentWaitlistButtonState;

    private static final String TAG = "EventInfoFragment";

    /**
     * Constructs an EventInfoFragment with the specified event and main events fragment reference.
     *
     * @param event The selected event to display details for.
     * @param eventsFragment Reference to the main fragment for editing actions.
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

    /**
     * Inflates the view with event details, sets up join/leave waitlist button, and shows the edit button if permitted.
     *
     * @param inflater The LayoutInflater object to inflate views in the fragment.
     * @param container The parent view the fragment's UI attaches to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a saved state.
     * @return The root View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        View view = inflater.inflate(R.layout.event_info_popup, null);

        TextView eventName = view.findViewById(R.id.popup_event_name_text);
        FloatingActionButton editEventButton = view.findViewById(R.id.popup_edit_event_info_button);
        Button waitlistButton = view.findViewById(R.id.popup_event_waitlist_button);
        ImageView eventImage = view.findViewById(R.id.popup_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_event_description_text);
        TextView eventEntrantsCount = view.findViewById(R.id.popup_create_event_max_entrants);

        eventName.setText(event.getEventName());

        eventDuration.setText(getString(
                R.string.event_duration,
                FormatDate.format(event.getStartDate()),
                FormatDate.format(event.getEndDate())
        ));
        eventRegistrationDeadline.setText(getString(
                R.string.registration_deadline,
                FormatDate.format(event.getDeadline())
        ));
        eventDescription.setText(event.getEventDescription());

        if (event.hasPoster()) {
            Glide.with(this)
                    .load(event.getPosterUri())
                    .into(eventImage);
            eventImage.setOnClickListener((v) -> {
                if (v.isClickable()) {
                    ImagesViewModel imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
                    imagesViewModel.setSelectedImage(event.getPosterUri());
                    new ImageInfoFragment().show(requireActivity().getSupportFragmentManager(), "fragment_image_info");
                }
            });
            eventImage.setClickable(true);
            eventImage.setFocusable(true);
        } else {
            eventImage.setClickable(false);
            eventImage.setFocusable(false);
            eventImage.setImageResource(R.drawable.default_event_poster);
        }

        observeEventSignups(eventEntrantsCount, waitlistButton);
        updateWaitlistButtonState(waitlistButton);

        // Set up waitlist button click listener
        waitlistButton.setOnClickListener(view1 -> {
            if(currentWaitlistButtonState == 1){ // leave waitlist
                leaveEventWaitlist(event);
                waitlistButton.setText(R.string.join_waitlist);
                currentWaitlistButtonState = 0;
            } else { // join waitlist
                joinEventWaitlist(event);
                waitlistButton.setText(R.string.leave_waitlist);
                currentWaitlistButtonState = 1;
            }
        });

        // Initialize edit event button if user can edit
        if(eventsViewModel.canEdit(event)){
            editEventButton.setOnClickListener(view12 -> eventsFragment.showEditEventPopup(event));
            editEventButton.setVisibility(View.VISIBLE);

            eventEntrantsCount.setOnClickListener(v -> {
                Log.d("EventInfoFragment", "clicked on eventEntrantsCount");
                navigateToEventEntrantsScreen();
            });
        }
        return view;
    }

    private void observeEventSignups(TextView eventEntrantsCount, Button waitlistButton) {
        if (event.getDocumentId() == null) {
            Log.e(TAG, "Event document ID is null");
            return;
        }

        LiveData<List<Signup>> signupsLiveData = eventsViewModel.getSignupsOfEvent(event.getDocumentId());

        signupsLiveData.observe(getViewLifecycleOwner(), signups -> {
            int entrantsCount = signups != null ? signups.size() : 0;
            int maxEntrants = event.getMaxEntrants();

            if (maxEntrants != -1) {
                eventEntrantsCount.setText(getString(
                        R.string.entrants_count_with_max,
                        entrantsCount,
                        maxEntrants));

                if (entrantsCount >= maxEntrants) {
                    if (!isAlreadyOnWaitlist(event)) {
                        waitlistButton.setEnabled(false);
                    }
                } else {
                    waitlistButton.setEnabled(true);
                }
            } else {
                eventEntrantsCount.setText(getString(R.string.entrants_count, entrantsCount));
                waitlistButton.setEnabled(true);
            }
        });
    }

    private void updateWaitlistButtonState(Button waitlistButton) {
        boolean isAlreadyInWaitlist = isAlreadyOnWaitlist(event);

        if (isAlreadyInWaitlist) {
            currentWaitlistButtonState = 1;
            waitlistButton.setText(R.string.leave_waitlist);
        } else {
            currentWaitlistButtonState = 0;
            waitlistButton.setText(R.string.join_waitlist);
        }
    }

    private void navigateToEventEntrantsScreen(){
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.navigation_view_entrants);
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
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
