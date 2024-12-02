package com.example.eventapp.ui.events;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.services.GetUserLocationService;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * This is the fragment that displays the information of the event corresponding to the qr code
 * that was scanned.
 */
public class ScannedEventFragment extends BottomSheetDialogFragment {

    private final String TAG = "ScannedEventFragment";
    private EventsViewModel eventsViewModel;
    private final Event event;
    private int currentWaitlistButtonState;
    private Button waitlistButton;
    private GetUserLocationService locationService;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    public ScannedEventFragment (Event event) {
        this.event = event;
    }

    /**
     * Initializes the location permission launcher when the view is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        fetchLocationAndJoinWaitlist();
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Sets the text to the correct values, sets onclick listeners for the buttons.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        View view = inflater.inflate(R.layout.scanned_event_popup, null);
        TextView eventName = view.findViewById(R.id.popup_scanned_event_name_text);
        waitlistButton = view.findViewById(R.id.popup_scanned_event_waitlist_button);
        ImageView eventImage = view.findViewById(R.id.popup_scanned_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_scanned_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_scanned_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_scanned_event_description_text);
        TextView eventEntrantsCount = view.findViewById(R.id.popup_scanned_event_max_entrants);

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

        boolean isAlreadyInWaitlist = isAlreadyOnWaitlist(event);

        // initial button text
        if(isAlreadyInWaitlist){
            currentWaitlistButtonState = 1;
            waitlistButton.setText("Leave Waitlist");
        }else{
            currentWaitlistButtonState = 0;
            waitlistButton.setText("Join Waitlist");
        }

        // Initialize location service
        locationService = new GetUserLocationService(requireContext());

        // Set up waitlist button click listener
        waitlistButton.setOnClickListener(view1 -> {
            if(currentWaitlistButtonState == 1){ // leave waitlist
                leaveEventWaitlist(event);
                waitlistButton.setText(R.string.join_waitlist);
                currentWaitlistButtonState = 0;
            } else { // join waitlist
                if (event.isGeolocationRequired()) {
                    checkAndRequestLocationPermission();
                } else {
                    joinEventWaitlist(event);
                    waitlistButton.setText(R.string.leave_waitlist);
                    currentWaitlistButtonState = 1;
                }
            }
        });

        return view;
    }

    /**
     * This method is called once we have location permissions
     * and joins the user with their location to the event with a signup
     */
    private void fetchLocationAndJoinWaitlist() {
        locationService.fetchUserLocation(requireActivity(), new GetUserLocationService.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                joinEventWaitlist(event, location.getLatitude(), location.getLongitude());
                waitlistButton.setText(R.string.leave_waitlist);
                currentWaitlistButtonState = 1;
                Toast.makeText(requireContext(), "This event uses your geolocation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks what stage of denial the user is in and asks for
     * permission accordingly
     */
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            fetchLocationAndJoinWaitlist();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // User previously denied permission
            showPermissionRationale();
        } else {
            // First time asking for permission
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Displays an alert stating the reason the user must allow location permissions
     * The user cannot get into geolocation required events without giving
     * location permissions
     */
    private void showPermissionRationale() {
        new AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Location Permission Needed")
                .setMessage("This event requires location permission to join.")
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Add user to waitlist of event without location
     *
     * @param event event to which the user is getting added
     */
    private void joinEventWaitlist(Event event){
        eventsViewModel.registerToEvent(event);
    }

    /**
     * Add user to waitlist of event with location
     *
     * @param event event to which the user is getting added
     */
    private void joinEventWaitlist(Event event, double lat, double lon){
        eventsViewModel.registerToEvent(event, lat, lon);
    }

    /**
     * Unregisters the user from the waitlist
     * Tells the view model to delete the signup
     * @param event The event that the user is leaving
     */
    private void leaveEventWaitlist(Event event){
        eventsViewModel.unregisterFromEvent(event);
    }

    /**
     * Checks if the user is already on the waitlist for a given event
     * @param event The event that needs to be checked
     * @return Boolean value of whether they user is signed up for the event
     */
    private boolean isAlreadyOnWaitlist(Event event){
        return eventsViewModel.isSignedUp(event);
    }
}