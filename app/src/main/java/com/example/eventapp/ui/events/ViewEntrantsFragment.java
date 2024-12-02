package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.example.eventapp.repositories.DTOs.SignupFilter;
import com.example.eventapp.repositories.DTOs.UserSignupEntry;
import com.example.eventapp.viewmodels.EntrantsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment for displaying the entrants for a given event
 * Has options to filter based on what list the entrant is on
 * Can display the map view if the given event has
 * geolocation turned on
 */
public class ViewEntrantsFragment extends Fragment implements NotificationMessageInputFragment.NotificationMessageInputListener {
    private ArrayList<UserSignupEntry> entrants;
    private EntrantsAdapter entrantsAdapter;
    private EntrantsViewModel entrantsViewModel;

    // Cancelled, Waitlisted, Chosen, Enrolled:
    private boolean[] filterOptions;

    @Override
    public void notifySelected(String messageContents){
        Log.d("ViewEntrantsFragment", "message contents were: " + messageContents);
        entrantsViewModel.notifyEntrants(getSelectedEntrants(), messageContents);
    }

    /**
     * This creates the view inflater for the view
     * @return the inflater for the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_entrants, container, false);
    }

    /**
     * This is used to create the entrant view
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
        filterOptions = new boolean[]{true, true, true, true};
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
        manageQrCodeButton.setOnClickListener(v -> showManageQrCodeFragment());

        // Notify Users Button
        ImageButton notifySelectedButton = view.findViewById(R.id.fragment_view_entrants_notify_selected_button);
        notifySelectedButton.setOnClickListener(view1 -> promptUserForNotificationMessage());

        // Cancel Selected Button
        ImageButton cancelSelectedButton = view.findViewById(R.id.fragment_view_entrants_cancel_selected_button);
        cancelSelectedButton.setOnClickListener(view12 -> cancelSelectedSignups());

        Event currentEvent = entrantsViewModel.getCurrentEventToQuery();
        if (currentEvent != null) {
            entrantsViewModel.setCurrentEventToQuery(currentEvent);
        } else {
            Log.e("ViewEntrantsFragment", "Current event is null");
        }

        // Show Map Button
        ImageButton showMap = view.findViewById(R.id.fragment_view_entrants_show_map);
        assert currentEvent != null;
        if (currentEvent.isGeolocationRequired()) {
            showMap.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), EventMapFragment.class);

                // Pass additional data if needed
                intent.putExtra("eventId", currentEvent.getDocumentId());
                startActivity(intent);
            });
        } else {
            showMap.setVisibility(View.GONE);
        }

        entrantsViewModel.getFilteredUserSignupEntriesLiveData().observe(getViewLifecycleOwner(), this::updateEntrantsList);
        updateFilter();
    }

    /**
     * Method for updating the entrants list
     * @param newEntrants Live data list of current entrants
     */
    private void updateEntrantsList(List<UserSignupEntry> newEntrants) {
        entrants.clear();
        entrants.addAll(newEntrants);
        // TODO: Use DiffUtil for better performance
        entrantsAdapter.notifyDataSetChanged();
    }

    /**
     * Method for showing the filter options popup
     */
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

    /**
     * Method for updating the applied filters
     */
    private void updateFilter() {
        SignupFilter signupFilter = new SignupFilter(
                filterOptions[0], // isCancelled
                filterOptions[1], // isWaitlisted
                filterOptions[2], // isChosen
                filterOptions[3]  // isEnrolled
        );

        entrantsViewModel.updateFilter(signupFilter);
    }

    /**
     * Method for showing the manage QR Fragment
     */
    private void showManageQrCodeFragment() {
        ManageQRCodeFragment manageQRCodeFragment = new ManageQRCodeFragment(entrantsViewModel.getCurrentEventToQuery());
        manageQRCodeFragment.show(requireActivity().getSupportFragmentManager(), "manage_qr_code");
    }

    // Use this like this:
    // Button notifyButton = view.findViewById(R.id.notify_selected_button);
    // notifyButton.setOnClickListener(v -> {
    //     List<UserSignupEntry> selectedEntrants = getSelectedEntrants();
    //     entrantsViewModel.notifyEntrants(selectedEntrants, messageContent);
    // });
    public List<UserSignupEntry> getSelectedEntrants() {
        List<UserSignupEntry> selectedEntrants = new ArrayList<>();
        for (UserSignupEntry entry : entrants) {
            if (entry.isSelected()) {
                selectedEntrants.add(entry);
            }
        }
        return selectedEntrants;
    }

    public void clearSelection() {
        for (UserSignupEntry entry : entrants) {
            entry.setSelected(false);
        }
        entrantsAdapter.notifyDataSetChanged();
    }

    private void promptUserForNotificationMessage() {
        NotificationMessageInputFragment notificationMessageInputFragment = new NotificationMessageInputFragment(this);
        notificationMessageInputFragment.show(requireActivity().getSupportFragmentManager(), "notification_message_input");
    }

    private void cancelSelectedSignups(){
        entrantsViewModel.cancelEntrants(getSelectedEntrants());
    }
}
