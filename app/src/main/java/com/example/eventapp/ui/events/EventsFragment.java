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

public class EventsFragment extends Fragment implements
        EventAdapter.OnEventClickListener {
    private EventsViewModel eventsViewModel;
    private ArrayList<Event> events;
    private EventAdapter eventAdapter;

    private FragmentEventsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        eventsViewModel = new ViewModelProvider(this).get(EventsViewModel.class);
        binding = FragmentEventsBinding.inflate(inflater, container, false);

        // final TextView textView = binding.textEvents;
        // eventsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        RecyclerView eventsGrid = view.findViewById(R.id.events_grid);

        // referenced https://stackoverflow.com/a/40587169 by Suragch, on October 23, 2024
        eventsGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));

        events = new ArrayList<>();
        eventAdapter = new EventAdapter(events, this);
        eventsGrid.setAdapter(eventAdapter);

        // observe events from ViewModel and update the adapter when data changes
        eventsViewModel.getEvents().observe(getViewLifecycleOwner(), this::updateEventList);

        FloatingActionButton createEventButton = view.findViewById(R.id.create_event_button);

        createEventButton.setOnClickListener((v) -> {
            // this is where you eventsViewModel.addEvent(newEvent);
            // or move this to a separate function
            // no need to notify the adapter of anything - updateEventList handles it
            Log.d("EventsFragment", "Create event button clicked");
        });

        // testing
        boolean isOrganizer = true; // adjust later
        if(!isOrganizer){
            createEventButton.setVisibility(View.GONE);
        }
    }

    private void updateEventList(ArrayList<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        // should change later, we should look into DiffUtil to not call raw notifyDataSetChanged
        // we could also create a .setEvents method in the adapter
        // to encapsulate all the logic there
        // eventAdapter.setEvents(newEvents);
        eventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEventClick(Event event) {
        Log.d("EventsFragment", "Event with name " + event.getEventName() + " clicked");
        // eventsViewModel.updateEvent(event);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}