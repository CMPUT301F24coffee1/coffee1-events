package com.example.eventapp.ui.events;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.Signup;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.viewmodels.EntrantsViewModel;
import com.example.eventapp.ui.images.ImageInfoFragment;
import com.example.eventapp.services.GetUserLocationService;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
    private Button waitlistButton;
    private GetUserLocationService locationService;
    private ActivityResultLauncher<String> locationPermissionLauncher;


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
        waitlistButton = view.findViewById(R.id.popup_event_waitlist_button);
        ImageView eventImage = view.findViewById(R.id.popup_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_event_description_text);
        Button eventEntrantsCount = view.findViewById(R.id.create_event_max_entrants);
        TextView eventEntrantsText = view.findViewById(R.id.create_event_max_entrants2);
        FloatingActionButton qrButton = view.findViewById(R.id.popup_event_qr_button);
        CardView facilityCard = view.findViewById((R.id.popup_event_facility_image_card));
        ImageView facilityImage = view.findViewById(R.id.popup_event_facility_image);
        TextView facilityName = view.findViewById((R.id.popup_event_facility_name));

        eventName.setText(event.getEventName());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(event.getStartDate());
        String startTime = format.format(calendar.getTime());
        calendar.setTimeInMillis(event.getEndDate());
        String endTime = format.format(calendar.getTime());
        eventDuration.setText(getString(
                R.string.event_duration,
                startTime,
                endTime
        ));
        calendar.setTimeInMillis(event.getDeadline());
        String deadLine = format.format(calendar.getTime());
        eventRegistrationDeadline.setText(getString(
                R.string.registration_deadline,
                deadLine)
        );
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
            eventImage.setImageBitmap(PhotoManager.generateDefaultPoster(event.getDocumentId()));
        }

        FacilityRepository facilityRepository = FacilityRepository.getInstance();
        facilityRepository.getFacilityById(event.getFacilityId()).thenAccept(facility -> {
            if (facility.hasPhoto()) {
                Glide.with(requireContext())
                        .load(facility.getPhotoUri())
                        .into(facilityImage);
                facilityCard.setOnClickListener((v) -> {
                    if (v.isClickable()) {
                        ImagesViewModel imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
                        imagesViewModel.setSelectedImage(facility.getPhotoUri());
                        new ImageInfoFragment().show(requireActivity().getSupportFragmentManager(), "fragment_image_info");
                    }
                });
                facilityCard.setClickable(true);
                facilityCard.setFocusable(true);
            } else {
                facilityCard.setClickable(false);
                facilityCard.setFocusable(false);
                facilityImage.setImageResource(R.drawable.ic_facility_24dp);
            }

            facilityName.setText(facility.getFacilityName());
        });

        observeEventSignups(eventEntrantsCount, waitlistButton);
        observeEventSignups(eventEntrantsText, waitlistButton);
        updateWaitlistButtonState(waitlistButton);

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

        // Initialize edit event button if user can edit
        if(eventsViewModel.canEdit(event)){
            editEventButton.setOnClickListener(view12 -> eventsFragment.showEditEventPopup(event));
            editEventButton.setVisibility(View.VISIBLE);

            eventEntrantsCount.setOnClickListener(v -> {
                Log.d("EventInfoFragment", "clicked on eventEntrantsCount");
                navigateToEventEntrantsScreen();
            });
            eventEntrantsCount.setVisibility(View.VISIBLE);
            eventEntrantsText.setVisibility(View.GONE);


            qrButton.setVisibility(View.VISIBLE);
            // Manage QR Code Button
            EntrantsViewModel entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
            entrantsViewModel.setCurrentEventToQuery(event);
            qrButton.setOnClickListener(v -> {
                ManageQRCodeFragment manageQRCodeFragment = new ManageQRCodeFragment(entrantsViewModel.getCurrentEventToQuery());
                manageQRCodeFragment.show(requireActivity().getSupportFragmentManager(), "manage_qr_code");
            });
        }
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
     * This method shows an error if the event id is null and
     *
     * @param eventEntrantsCount The number of entrants in the event
     * @param waitlistButton The button to join the waitlist
     */
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

    /**
     * This method updates the waitlist button to switch states
     * @param waitlistButton the button that is used to join/leave the waitlist
     */
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

    /**
     * This is the method to navigate to the event entrants screen
     * This occurs when the event entrants field is clicked
     */
    private void navigateToEventEntrantsScreen(){
        EntrantsViewModel entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
        NavController navController = NavHostFragment.findNavController(this);
        entrantsViewModel.setCurrentEventToQuery(event);
        navController.navigate(R.id.navigation_view_entrants);
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    /**
     * Joins the user to the waitlist with null location
     * Done by adding a signup object trough the events view model
     * Used for events that do not require geolocation
     * @param event This is the event that the user is joining
     */
    private void joinEventWaitlist(Event event){
        eventsViewModel.registerToEvent(event);
    }

    /**
     * Joins the user to the waitlist with location
     * Done by adding a signup object trough the events view model
     * Used for events that do require geolocation
     * @param event This is the event that the user is joining
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
