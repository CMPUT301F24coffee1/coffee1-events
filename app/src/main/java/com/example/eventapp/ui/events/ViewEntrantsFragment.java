package com.example.eventapp.ui.events;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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
 * Has options to filter based on the status of the entrants
 * The user can navigate to the map view if the event has geolocation turned on.
 */
public class ViewEntrantsFragment extends Fragment implements NotificationMessageInputFragment.NotificationMessageInputListener, LotteryDrawCountInputFragment.LotteryDrawCountInputListener {
    private ArrayList<UserSignupEntry> entrants;
    private EntrantsAdapter entrantsAdapter;
    private EntrantsViewModel entrantsViewModel;

    // Cancelled, Waitlisted, Chosen, Enrolled:
    private boolean[] filterOptions;

    /**
     * Draws entrants (for the invitation to enroll)
     * @param drawCount the number of entrants being drawn
     */
    @Override
    public void lotteryDraw(int drawCount){
        entrantsViewModel.drawEntrants(drawCount).thenAccept(resultString -> {
            Toast.makeText(getContext(), resultString, Toast.LENGTH_LONG).show();
        }).exceptionally(throwable -> {
            Toast.makeText(getContext(), "Lottery run failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        });
    }

    /**
     * Sends a custom notification to the selected entrants.
     *
     * @param messageContents the contents of the notification to be sent to the entrants
     */
    @Override
    public void notifySelected(String messageContents){
        Log.d("ViewEntrantsFragment", "message contents were: " + messageContents);
        List<UserSignupEntry> selectedEntrants = getSelectedEntrants();

        if (selectedEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No users selected", Toast.LENGTH_LONG).show();
            return;
        }
        entrantsViewModel.notifyEntrants(selectedEntrants, messageContents);
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
        ImageButton cancelSelectedButton = view.findViewById(R.id.fragment_view_entrants_delete_selected_button);
        cancelSelectedButton.setOnClickListener(view12 -> cancelSelectedSignups());

        // Lottery Button
        ImageButton lotteryButton = view.findViewById(R.id.fragment_view_entrants_draw_button);
        if(entrantsViewModel.getCurrentEventToQuery().isLotteryProcessed()){
            int enrolledCount = getEnrolledCount();
            int maxEnrolledSize = entrantsViewModel.getCurrentEventToQuery().getNumberOfAttendees();
            if(enrolledCount < maxEnrolledSize){
                lotteryButton.setOnClickListener(view15 -> {
                    askForLotteryDrawCount(maxEnrolledSize-enrolledCount);
                });
            }else{
                lotteryButton.setOnClickListener(view13 -> Toast.makeText(getContext(), "Enrollment is Full", Toast.LENGTH_SHORT).show());
            }
        }else{
            lotteryButton.setOnClickListener(view14 -> askForLotteryDrawCount(entrantsViewModel.getCurrentEventToQuery().getNumberOfAttendees()));
        }

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

    /**
     * Used to get the selected entrants.
     *
     * @return a list of UserSignupEntries
     */
    public List<UserSignupEntry> getSelectedEntrants() {
        List<UserSignupEntry> selectedEntrants = new ArrayList<>();
        for (UserSignupEntry entry : entrants) {
            if (entry.isSelected()) {
                selectedEntrants.add(entry);
            }
        }
        return selectedEntrants;
    }

    /**
     * Get the number of entrants that are enrolled in the event.
     *
     * @return the number of entrants that are enrolled in the event
     */
    public int getEnrolledCount() {
        int enrolledCount = 0;
        for (UserSignupEntry entry : entrants) {
            String status = entry.getAttendanceStatus();
            if(status.equals("Enrolled")){
                enrolledCount++;
            }
        }
        return enrolledCount;
    }

    /**
     * Clear the selected status from all entrants
     */
    public void clearSelection() {
        for (UserSignupEntry entry : entrants) {
            entry.setSelected(false);
        }
        entrantsAdapter.notifyDataSetChanged();
    }

    /**
     * Show a popup to the user, prompting them to input the message they wish to send to the selected
     * entrants
     */
    private void promptUserForNotificationMessage() {
        NotificationMessageInputFragment notificationMessageInputFragment = new NotificationMessageInputFragment(this);
        notificationMessageInputFragment.show(requireActivity().getSupportFragmentManager(), "notification_message_input");
    }

    /**
     * Get rid of the selected entrants' signups
     */
    private void cancelSelectedSignups(){
        entrantsViewModel.cancelEntrants(getSelectedEntrants());
    }

    /**
     * Show a popup to the user, prompting them to input the number of entrants they want to draw for.
     *
     * @param spaceRemaining the remaining number of entrants that can enroll (that the event has space for)
     */
    private void askForLotteryDrawCount(int spaceRemaining){
        LotteryDrawCountInputFragment lotteryDrawCountInputFragment = new LotteryDrawCountInputFragment(this, spaceRemaining);
        lotteryDrawCountInputFragment.show(requireActivity().getSupportFragmentManager(), "lottery_draw_count_input");
    }
}
