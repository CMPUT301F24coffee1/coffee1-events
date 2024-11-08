package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.models.Event;
import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentEventsBinding;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * The EventsFragment class is responsible for displaying and managing the user's events,
 * including events they organize and events they are signed up for. This fragment implements
 * multiple listeners for event-related actions, such as creating, editing, and deleting events,
 * as well as viewing event information.
 *
 * This fragment uses the EventsViewModel to observe changes to organized and signed-up events
 * and updates the corresponding RecyclerViews. It provides options to add new events, view
 * detailed event information, and modify existing events. The EventsFragment also handles
 * displaying the appropriate UI elements for event actions, including popups for creating,
 * editing, and viewing event details.
 *
 * Implements:
 * - {@link EventAdapter.OnEventClickListener}: to handle clicks on event items in RecyclerViews.
 * - {@link EventInfoFragment.EditEventInfoListener}: to handle editing from the event info popup.
 * - {@link CreateEventFragment.CreateEventListener}: to handle creation of a new event.
 * - {@link EditEventFragment.EditEventListener}: to handle saving or deleting edits on an event.
 */
public class EventsFragment extends Fragment implements
        EventAdapter.OnEventClickListener, EventInfoFragment.EditEventInfoListener, CreateEventFragment.CreateEventListener, EditEventFragment.EditEventListener {

    private EventsViewModel eventsViewModel;
    private ArrayList<Event> organizedEvents;
    private ArrayList<Event> signedUpEvents;
    private EventAdapter organizedEventsAdapter;
    private EventAdapter signedUpEventsAdapter;
    private CreateEventFragment currentCreateEventFragment;
    private EditEventFragment currentEditEventFragment;
    private EventInfoFragment currentEventInfoFragment;

    private FragmentEventsBinding binding;

    @Override
    public void createEvent(Event event) {
        eventsViewModel.addEvent(event);
        currentCreateEventFragment.dismiss();
    }

    @Override
    public void saveEditedEvent(Event updatedEvent) {
        eventsViewModel.updateEvent(updatedEvent);
        currentEditEventFragment.dismiss();
        currentEventInfoFragment.dismiss();
        Log.d("EventsFragment", "Event edited: " + updatedEvent.getEventName());
    }

    @Override
    public void deleteEvent(Event event){
        eventsViewModel.removeEvent(event);
        currentEditEventFragment.dismiss();
        currentEventInfoFragment.dismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up RecyclerView for organized events
        RecyclerView organizedEventsGrid = view.findViewById(R.id.organized_events_grid);
        organizedEventsGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        organizedEvents = new ArrayList<>();
        organizedEventsAdapter = new EventAdapter(organizedEvents, this);
        organizedEventsGrid.setAdapter(organizedEventsAdapter);

        // set up RecyclerView for signed-up events
        RecyclerView signedUpEventsGrid = view.findViewById(R.id.signed_up_events_grid);
        signedUpEventsGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        signedUpEvents = new ArrayList<>();
        signedUpEventsAdapter = new EventAdapter(signedUpEvents, this);
        signedUpEventsGrid.setAdapter(signedUpEventsAdapter);

        eventsViewModel.getOrganizedEvents().observe(getViewLifecycleOwner(), this::updateOrganizedEventsList);
        eventsViewModel.getSignedUpEvents().observe(getViewLifecycleOwner(), this::updateSignedUpEventsList);

        FloatingActionButton createEventButton = view.findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(v -> {
            Log.d("EventsFragment", "Create event button clicked");
            showCreateEventPopup();
        });

        boolean isOrganizer = true;
        if (!isOrganizer) {
            createEventButton.setVisibility(View.GONE);
        }
    }

    private void updateOrganizedEventsList(List<Event> newOrganizedEvents) {
        organizedEvents.clear();
        organizedEvents.addAll(newOrganizedEvents);
        // TODO: calculating diff with DiffUtil
        organizedEventsAdapter.notifyDataSetChanged();
    }

    private void updateSignedUpEventsList(List<Event> newSignedUpEvents) {
        signedUpEvents.clear();
        signedUpEvents.addAll(newSignedUpEvents);
        // TODO: calculating diff with DiffUtil
        signedUpEventsAdapter.notifyDataSetChanged();
    }

    private void showCreateEventPopup() {
        CreateEventFragment createEventFragment = new CreateEventFragment(this);
        currentCreateEventFragment = createEventFragment;
        createEventFragment.show(getActivity().getSupportFragmentManager(), "create_event");
    }

    @Override
    public void onEventClick(Event event) {
        Log.d("EventsFragment", "Event with name " + event.getEventName() + " clicked");
        showEventInfoPopup(event);
    }

    private void showEventInfoPopup(Event event) {
        EventInfoFragment eventInfoFragment = new EventInfoFragment(event, this);
        currentEventInfoFragment = eventInfoFragment;
        eventInfoFragment.show(getActivity().getSupportFragmentManager(), "event_info");
    }

    @Override
    public void editEventInfo(Event event) {
        showEditEventPopup(event);
    }

    public void showEditEventPopup(Event event) {
        EditEventFragment editEventFragment = new EditEventFragment(event, this);
        currentEditEventFragment = editEventFragment;
        editEventFragment.show(getActivity().getSupportFragmentManager(), "edit_event");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}