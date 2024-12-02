package com.example.eventapp.ui.events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.Facility;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A bottom sheet fragment that provides a form for creating a new event.
 * This fragment allows users to input event details, select a facility, specify the number of attendees,
 * set event dates, and upload an optional poster image. It directly interacts with the EventsViewModel
 * to add the new event to the system.
 */
public class CreateEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {
    private EventsViewModel eventsViewModel;
    private String posterUriString = "";
    private Uri selectedPhotoUri;
    private ImageView posterImageView;
    private ArrayList<Long> timestamps;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    private Spinner facilitySpinner;
    private EditText numberOfAttendeesEditText;
    private List<Facility> facilitiesList;
    private ArrayAdapter<String> facilitiesAdapter;

    private EditText eventName;
    private EditText eventDescription;
    private CheckBox geolocationRequired;
    private Button createEventButton;
    private EditText maxEventEntrants;
    private Button eventDurationButton;
    private Button eventRegistrationDeadlineButton;
    private Button selectPosterButton;

    public CreateEventFragment() {}

    /**
     * Sets the selected timestamp for the specified date type.
     * @param timestamp the selected date in milliseconds since the epoch.
     * @param type      an integer representing the type of date:
     *                  0 for start date,
     *                  1 for end date,
     *                  2 for deadline date.
     */
    @Override
    public void setDate(long timestamp, int type) {
        switch (type) {
            case 0: // startTimestamp
                timestamps.set(0, timestamp);
                break;
            case 1: // endTimestamp
                timestamps.set(1, timestamp);
                break;
            case 2: // deadlineTimestamp
                timestamps.set(2, timestamp);
                break;
        }
        Log.d("CreateEventsFragment", "timestamp set to: " + timestamp);
        Log.d("CreateEventsFragment", "type: " + type);
    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        timestamps = new ArrayList<>(Arrays.asList(0L, 0L, 0L)); // (startTimestamp, endTimestamp, deadlineTimestamp)
    }

    /**
     * Inflates the layout for the fragment and initializes UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.create_event_popup, container, false);

        initViewComponents(view);
        setupFacilitySpinner();
        setupPhotoPicker();
        setupEventDurationButton();
        setupEventRegistrationDeadlineButton();
        setupCreateEventButton();

        return view;
    }

    /**
     * Initializes UI components by finding views by their IDs.
     *
     * @param view the root view of the fragment's layout.
     */
    private void initViewComponents(View view) {
        eventName = view.findViewById(R.id.popup_create_event_name);
        eventDescription = view.findViewById(R.id.popup_create_event_description);
        geolocationRequired = view.findViewById(R.id.popup_create_event_geolocation_checkbox);
        createEventButton = view.findViewById(R.id.popup_create_event_button);
        maxEventEntrants = view.findViewById(R.id.popup_create_event_max_entrants);
        eventDurationButton = view.findViewById(R.id.popup_create_event_duration_button);
        eventRegistrationDeadlineButton = view.findViewById(R.id.popup_create_event_registration_deadline_button);
        selectPosterButton = view.findViewById(R.id.popup_create_event_add_poster);
        posterImageView = view.findViewById(R.id.popup_create_event_image);
        facilitySpinner = view.findViewById(R.id.popup_create_event_facility_spinner);
        numberOfAttendeesEditText = view.findViewById(R.id.popup_create_event_number_of_attendees);

        posterImageView.setImageResource(R.drawable.default_event_poster);

        // Disable the Spinner until facilities are loaded
        facilitySpinner.setEnabled(false);
    }

    /**
     * Sets up the facility spinner by initializing the adapter and observing the facilities data.
     */
    private void setupFacilitySpinner() {
        facilitiesList = new ArrayList<>();
        facilitiesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        facilitiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facilitySpinner.setAdapter(facilitiesAdapter);

        eventsViewModel.getUserFacilities().observe(getViewLifecycleOwner(), newFacilitiesList -> {
            facilitiesList.clear();
            facilitiesList.addAll(newFacilitiesList);

            List<String> facilityNames = new ArrayList<>();
            for (Facility facility : facilitiesList) {
                facilityNames.add(facility.getFacilityName());
            }
            facilitiesAdapter.clear();
            facilitiesAdapter.addAll(facilityNames);
            facilitiesAdapter.notifyDataSetChanged();

            // Data is loaded
            facilitySpinner.setEnabled(true);
        });
    }

    /**
     * Sets up the photo picker functionality for selecting an event poster.
     */
    private void setupPhotoPicker() {
        PhotoPicker.PhotoPickerCallback pickerCallback = photoUri -> {
            selectedPhotoUri = photoUri;
            Glide.with(requireView()).load(selectedPhotoUri).into(posterImageView);
        };

        photoPickerLauncher = PhotoPicker.getPhotoPickerLauncher(this, pickerCallback);

        selectPosterButton.setOnClickListener(v -> PhotoPicker.openPhotoPicker(photoPickerLauncher));
    }

    /**
     * Sets up the event duration button to open date pickers for start and end dates.
     */
    private void setupEventDurationButton() {
        eventDurationButton.setOnClickListener(v -> {
            Log.d("CreateEventFragment", "set event duration button clicked");
            // Closes and opens instantly, add/override animations later
            showDatePickerFragment(1);
            showDatePickerFragment(0);
        });
    }

    /**
     * Sets up the registration deadline button to open a date picker for the deadline date.
     */
    private void setupEventRegistrationDeadlineButton() {
        eventRegistrationDeadlineButton.setOnClickListener(v -> {
            Log.d("CreateEventFragment", "set event deadline button clicked");
            showDatePickerFragment(2);
        });
    }

    /**
     * Sets up the create event button with validation and event creation logic.
     */
    private void setupCreateEventButton() {
        createEventButton.setOnClickListener(v -> {
            Log.d("CreateEventFragment", "Create Button Clicked");
            if (!validateInputs()) {
                return;
            }
            createEventButton.setFocusable(false);

            String newEventName = eventName.getText().toString();
            String newEventDescription = eventDescription.getText().toString();
            String maxEntrantsStr = maxEventEntrants.getText().toString();
            int maxEntrants = parseMaxEntrants(maxEntrantsStr);
            int numberOfAttendees = Integer.parseInt(numberOfAttendeesEditText.getText().toString());
            Facility selectedFacility = facilitiesList.get(facilitySpinner.getSelectedItemPosition());

            Event newEvent = new Event(
                    newEventName,
                    "",
                    newEventDescription,
                    numberOfAttendees,
                    geolocationRequired.isChecked(),
                    maxEntrants,
                    timestamps.get(0),
                    timestamps.get(1),
                    timestamps.get(2));

            newEvent.setFacilityId(selectedFacility.getDocumentId());

            if (selectedPhotoUri == null) {
                eventsViewModel.addEvent(newEvent);
                dismiss();
            } else {
                uploadPhotoAndCreateEvent(newEvent);
            }
        });
    }

    /**
     * Validates user inputs and shows appropriate error messages.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        // Validate event name
        if (eventName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Must have event name", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate number of attendees
        String numberOfAttendeesStr = numberOfAttendeesEditText.getText().toString();
        if (numberOfAttendeesStr.isEmpty()) {
            Toast.makeText(getContext(), "Must enter number of attendees", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int numberOfAttendees = Integer.parseInt(numberOfAttendeesStr);
            if (numberOfAttendees < 1) {
                Toast.makeText(getContext(), "Number of attendees must be at least 1", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number of attendees", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate timestamps
        if (timestamps.get(0) == 0 || timestamps.get(1) == 0 || timestamps.get(2) == 0) {
            Toast.makeText(getContext(), "All event dates must be set", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate facility selection
        if (facilitySpinner.getSelectedItemPosition() < 0 || facilitySpinner.getSelectedItemPosition() >= facilitiesList.size()) {
            Toast.makeText(getContext(), "Please select a facility", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Parses the maximum number of entrants from a string input.
     *
     * @param maxEntrantsStr the string input for maximum entrants.
     * @return the parsed integer value, or -1 if invalid.
     */
    private int parseMaxEntrants(String maxEntrantsStr) {
        int maxEntrants = -1;
        if (!maxEntrantsStr.isEmpty()) {
            try {
                maxEntrants = Integer.parseInt(maxEntrantsStr);
                if (maxEntrants <= 0) {
                    maxEntrants = -1; // Treat zero or negative as no limit
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return maxEntrants;
    }

    /**
     * Uploads the selected photo to Firebase Storage and creates the event upon success.
     *
     * @param newEvent the Event object to be created after the photo upload.
     */
    private void uploadPhotoAndCreateEvent(Event newEvent) {
        PhotoManager.uploadPhotoToFirebase(
                getContext(),
                selectedPhotoUri,
                75,
                "events",
                "poster",
                new PhotoManager.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        posterUriString = downloadUrl;
                        Log.d("PhotoUploader", "Photo uploaded successfully: " + posterUriString);

                        newEvent.setPosterUriString(posterUriString);
                        eventsViewModel.addEvent(newEvent);
                        dismiss();
                    }

                    @Override
                    public void onUploadFailure(Exception e) {
                        Log.e("PhotoUploader", "Upload failed", e);
                        Toast.makeText(getContext(), "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        createEventButton.setFocusable(true);
                    }
                });
    }

    /**
     * Displays a date picker dialog for the user to select a date.
     * The type parameter specifies whether the date is for the start, end, or deadline.
     *
     * @param type an integer representing the type of date:
     *             0 for start date,
     *             1 for end date,
     *             2 for deadline date.
     */
    private void showDatePickerFragment(int type) {
        DatePickerFragment datePickerFragment = new DatePickerFragment(this, type);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
