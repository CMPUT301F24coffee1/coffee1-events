package com.example.eventapp.ui.events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.models.Event;
import com.example.eventapp.R;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * EditEventFragment is a BottomSheetDialogFragment that provides an interface for users
 * to edit an existing event's details, such as name, description, duration, registration deadline,
 * geolocation requirements, max entrants, and poster image.
 * <p>
 * The fragment allows users to select and upload a new poster image, set event dates through
 * a date picker, and save or delete the event. It communicates updates to the main event fragment
 * using an {@link EditEventListener} interface.
 * <p>
 * Implements the {@link DatePickerFragment.SetDateListener} interface to handle selected dates.
 */
public class EditEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {
    private EventsViewModel eventsViewModel;
    private Event event;
    private String posterUriString;
    private Uri oldPosterUri;
    private Uri selectedPhotoUri;
    private ImageView posterImageView;
    private ArrayList<Long> timestamps;
    private EditEventListener editEventListener;

    /**
     * Interface for main event fragment to implement in order to save edited event
     */
    interface EditEventListener {
        void saveEditedEvent(Event event);
        void deleteEvent(Event event);
    }

    /**
     * Constructor for the edit event fragment
     * @param event The event that is going to be edited
     * @param listener A listener to transfer the data to the parent fragment
     */
    public EditEventFragment(Event event, EditEventListener listener) {
        this.event = event;
        this.editEventListener = listener;
        this.oldPosterUri = event.getPosterUri();
    }

    /**
     *
     * @param timestamp A l
     * @param type
     */
    @Override
    public void setDate(long timestamp, int type) {
        switch(type) {
            case 0: timestamps.set(0, timestamp); break;
            case 1: timestamps.set(1, timestamp); break;
            case 2: timestamps.set(2, timestamp); break;
        }
    }

    /**
     * Initialize and run the edit fragment with current event info
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return listener with new event
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        timestamps = new ArrayList<>(Arrays.asList(event.getStartDate(), event.getEndDate(), event.getDeadline()));
        View view = inflater.inflate(R.layout.edit_event_popup, container, false);

        EditText eventName = view.findViewById(R.id.popup_edit_event_name);
        EditText eventDescription = view.findViewById(R.id.popup_edit_event_description);
        CheckBox geolocationRequired = view.findViewById(R.id.popup_edit_event_geolocation_checkbox);
        EditText maxEventEntrants = view.findViewById(R.id.popup_edit_event_max_entrants);
        Button saveEventButton = view.findViewById(R.id.popup_save_event_button);
        Button eventDurationButton = view.findViewById(R.id.popup_edit_event_duration_button);
        Button eventRegistrationDeadlineButton = view.findViewById(R.id.popup_edit_event_registration_deadline_button);
        Button selectPosterButton = view.findViewById(R.id.popup_edit_event_add_poster);
        Button deleteEventButton = view.findViewById(R.id.popup_edit_event_delete_event_button);
        posterImageView = view.findViewById(R.id.popup_edit_event_image);

        // Initialize with current event data
        eventName.setText(event.getEventName());
        eventDescription.setText(event.getEventDescription());
        geolocationRequired.setChecked(event.isGeolocationRequired());
        maxEventEntrants.setText(event.getMaxEntrants() == -1 ? "" : String.valueOf(event.getMaxEntrants()));
        if (event.hasPoster()) {
            Glide.with(this).load(event.getPosterUri()).into(posterImageView);
        } else {
            posterImageView.setImageResource(R.drawable.default_event_poster);
        }

        // Implement event duration and registration deadline buttons
        eventDurationButton.setOnClickListener(v -> {
            showDatePickerFragment(1);
            showDatePickerFragment(0);
        });
        eventRegistrationDeadlineButton.setOnClickListener(v -> showDatePickerFragment(2));

        // Get new photo if selected
        PhotoPicker.PhotoPickerCallback pickerCallback = new PhotoPicker.PhotoPickerCallback() {
            @Override
            public void onPhotoPicked(Uri photoUri) {
                // Save the URI for later use after validation
                selectedPhotoUri = photoUri;
                Glide.with(requireView()).load(selectedPhotoUri).into(posterImageView);
            }
        };

        ActivityResultLauncher<Intent> photoPickerLauncher = PhotoPicker.getPhotoPickerLauncher(this, pickerCallback);

        // Set listeners
        selectPosterButton.setOnClickListener(v -> PhotoPicker.openPhotoPicker(photoPickerLauncher));

        if(eventsViewModel.isUserOrganizerOrAdmin()) {
            // delete button
            deleteEventButton.setVisibility(View.VISIBLE);
            deleteEventButton.setOnClickListener(view1 -> editEventListener.deleteEvent(event));
        }

        // Save button
        saveEventButton.setOnClickListener(v -> {
            String newName = eventName.getText().toString();
            String newDescription = eventDescription.getText().toString();
            String maxEntrants = maxEventEntrants.getText().toString();

            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Event name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update event details
            event.setEventName(newName);
            event.setEventDescription(newDescription);
            event.setGeolocationRequired(geolocationRequired.isChecked());
            event.setStartDate(timestamps.get(0));
            event.setEndDate(timestamps.get(1));
            event.setDeadline(timestamps.get(2));

            if (!maxEntrants.isEmpty()) {
                try {
                    event.setMaxEntrants(Integer.parseInt(maxEntrants));
                } catch (NumberFormatException e) {
                    event.setMaxEntrants(-1); // Reset if invalid
                }
            }

            // Handle new poster upload if selected
            if (selectedPhotoUri != null) {
                PhotoManager.UploadCallback uploadCallback = new PhotoManager.UploadCallback()
                {
                    @Override
                    public void onUploadSuccess (String downloadUrl){
                        event.setPosterUriString(downloadUrl);


                        editEventListener.saveEditedEvent(event);
                    }

                    @Override
                    public void onUploadFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    }
                };

                if (oldPosterUri == null) {
                    PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "events", "poster", uploadCallback);
                } else {
                    final String id = Objects.requireNonNull(oldPosterUri.getLastPathSegment()).split("/")[1];
                    PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "events", "poster", id, uploadCallback);
                }
            } else {
                editEventListener.saveEditedEvent(event);
            }
        });

        return view;
    }

    /**
     * Open the date picker fragments
     * @param type Defines the type of date (0: start, 1: end, 2: deadline)
     */
    private void showDatePickerFragment(int type) {
        DatePickerFragment datePickerFragment = new DatePickerFragment(this, type);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
