package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.databinding.FragmentEventsBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    EventsListAdapter eventsListAdapter;
    ViewPager2 viewPager;

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
        organizedEvents = new ArrayList<>();
        organizedEventsAdapter = new EventAdapter(organizedEvents, this);

        // set up RecyclerView for signed-up events
        signedUpEvents = new ArrayList<>();
        signedUpEventsAdapter = new EventAdapter(signedUpEvents, this);

        eventsListAdapter = new EventsListAdapter(organizedEventsAdapter, signedUpEventsAdapter);
        viewPager = view.findViewById(R.id.events_viewpager);
        viewPager.setAdapter(eventsListAdapter);

        eventsViewModel.getOrganizedEvents().observe(getViewLifecycleOwner(), this::updateOrganizedEventsList);
        eventsViewModel.getSignedUpEvents().observe(getViewLifecycleOwner(), this::updateSignedUpEventsList);

        TabLayout tabLayout = view.findViewById(R.id.events_tabs);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? getString(R.string.events) : getString(R.string.organized_events )))
                .attach();

        FloatingActionButton createEventButton = view.findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(v -> {
            Log.d("EventsFragment", "Create event button clicked");
            showCreateEventPopup();
        });

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                if (user.isOrganizer()) {
                    createEventButton.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    eventsListAdapter.setItemCount(2);
                    new TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> tab.setText(position == 0 ? getString(R.string.events) : getString(R.string.organized_events )))
                            .attach();
                } else {
                    createEventButton.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    eventsListAdapter.setItemCount(1);
                }
            }
        });

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