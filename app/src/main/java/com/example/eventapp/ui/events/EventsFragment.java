package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentEventsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    ArrayList<Event> events;
    RecyclerView eventsGrid;
    EventAdapter eventAdapter;

    private FragmentEventsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventsViewModel eventsViewModel =
                new ViewModelProvider(this).get(EventsViewModel.class);

        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textEvents;
        //eventsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        //testing
        boolean isOrganizer = true; //adjust later
        events = new ArrayList<>();
        events.add(new Event("event1"));
        events.add(new Event("event2"));
        eventsGrid = view.findViewById(R.id.events_grid);

        //referenced https://stackoverflow.com/a/40587169 by Suragch, on October 23, 2024
        eventsGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        eventAdapter = new EventAdapter(events);
        eventsGrid.setAdapter(eventAdapter);

        FloatingActionButton createEventButton = view.findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("EventsFragment", "Create event button clicked");

            }
        });

        if(!isOrganizer){
            createEventButton.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}