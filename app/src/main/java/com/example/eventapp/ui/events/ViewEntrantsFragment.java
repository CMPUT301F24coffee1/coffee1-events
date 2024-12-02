package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.SignupFilter;
import com.example.eventapp.viewmodels.EntrantsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewEntrantsFragment extends Fragment {
    private ArrayList<User> entrants;
    private EntrantsAdapter entrantsAdapter;
    private EntrantsViewModel entrantsViewModel;

    // Cancelled, Waitlisted, Chosen, Enrolled:
    private boolean[] filterOptions;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_entrants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel and data
        entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
        filterOptions = new boolean[]{false, false, false, false};
        entrants = new ArrayList<>();
        entrantsAdapter = new EntrantsAdapter(entrants);

        // Set up RecyclerView
        RecyclerView entrantsList = view.findViewById(R.id.fragment_view_entrants_entrant_list);
        entrantsList.setLayoutManager(new LinearLayoutManager(getContext()));
        entrantsList.setAdapter(entrantsAdapter);

        // Handle filter button
        ImageButton filterOptionsButton = view.findViewById(R.id.fragment_view_entrants_filter_options_button);
        filterOptionsButton.setOnClickListener(v -> showFilterOptionsPopup());

        // Manage QR Code Button
        ImageButton manageQrCodeButton = view.findViewById(R.id.fragment_view_entrants_qr_code_button);
        manageQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showManageQrCodeFragment();
            }
        });

        Event currentEvent = entrantsViewModel.getCurrentEventToQuery();
        if (currentEvent != null) {
            entrantsViewModel.setCurrentEventToQuery(currentEvent);
        } else {
            Log.e("ViewEntrantsFragment", "Current event is null");
        }

        entrantsViewModel.getFilteredUsersLiveData().observe(getViewLifecycleOwner(), this::updateEntrantsList);
        updateFilter();
    }

    private void updateEntrantsList(List<User> newEntrants) {
        entrants.clear();
        entrants.addAll(newEntrants);
        // TODO: calculating diff with DiffUtil
        entrantsAdapter.notifyDataSetChanged();
    }

    private void showFilterOptionsPopup() {
        CharSequence[] options = {"Cancelled", "Waitlisted", "Chosen", "Enrolled"};
        boolean[] tempFilterOptions = Arrays.copyOf(filterOptions, filterOptions.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter Options")
                .setMultiChoiceItems(options, tempFilterOptions, (dialogInterface, i, b) -> {})
                .setPositiveButton("Apply", (dialog, id) -> {
                    filterOptions = Arrays.copyOf(tempFilterOptions, tempFilterOptions.length);
                    updateFilter();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                })
                .show();
    }

    private void updateFilter() {
        SignupFilter signupFilter = new SignupFilter(
                filterOptions[0] ? true : null, // isCancelled
                filterOptions[1] ? true : null, // isWaitlisted
                filterOptions[2] ? true : null, // isChosen
                filterOptions[3] ? true : null  // isEnrolled
        );

        entrantsViewModel.updateFilter(signupFilter);
    }

    private void showManageQrCodeFragment() {
        ManageQRCodeFragment manageQRCodeFragment = new ManageQRCodeFragment(entrantsViewModel.getCurrentEventToQuery());
        manageQRCodeFragment.show(requireActivity().getSupportFragmentManager(), "manage_qr_code");
    }
}
