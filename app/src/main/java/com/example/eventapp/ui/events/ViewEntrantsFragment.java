package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;

public class ViewEntrantsFragment extends Fragment {

    private RecyclerView entrantsList;
    private ArrayList<User> entrants;
    private boolean[] filterOptions;
    private final String TEST_EVENT_ID = "yZAbcFApEPz5kxl6kVBw";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_view_entrants, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d("ViewEntrantsFragment", "created");
        filterOptions = new boolean[]{true, true, true, true};
        entrantsList = view.findViewById(R.id.fragment_view_entrants_entrant_list);
        entrantsList.setLayoutManager(new LinearLayoutManager(getContext()));
        entrants = new ArrayList<>();

        ImageButton filterOptionsButton = view.findViewById(R.id.fragment_view_entrants_filter_options_button);
        ImageButton showMap = view.findViewById(R.id.fragment_view_entrants_notify_all_button);

        filterOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterOptionsPopup();
            }
        });

        // Set up a click listener if event has geolocation enabled (replace true with event.hasGeolocation())
        if (true) {
            showMap.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), EventMapFragment.class);

                // Pass additional data if needed
                intent.putExtra("eventId", TEST_EVENT_ID);
                startActivity(intent);
            });
        }

        // for testing
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        User actualUser = UserRepository.getInstance().getCurrentUserLiveData().getValue();
        entrants.add(actualUser);
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));
        entrants.add(new User("abc", "def"));

        EntrantsAdapter entrantsAdapter = new EntrantsAdapter(entrants);
        entrantsList.setAdapter(entrantsAdapter);
    }

    private void showFilterOptionsPopup(){

        CharSequence[] options = {"Cancelled", "Waitlisted", "Chosen", "Enrolled"};

        boolean[] tempFilterOptions = Arrays.copyOf(filterOptions, filterOptions.length);

        // referenced the Android developer docs
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter Options").setMultiChoiceItems(options, tempFilterOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                Log.d("ViewEntrantsFragment", "Updating Value");
            }
        }).setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id){
                Log.d("ViewEntrantsFragment", "Updating filterOptions to:" + Arrays.toString(tempFilterOptions));
                filterOptions = tempFilterOptions;
                Log.d("ViewEntrantsFragment", "filterOptions is now: " + Arrays.toString(filterOptions));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("ViewEntrantsFragment", "Discarding Changes");
            }
        }).show();
    }
}
