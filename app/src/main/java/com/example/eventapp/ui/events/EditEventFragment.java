package com.example.eventapp.ui.events;

import static android.content.ContentValues.TAG;

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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.models.Event;
import com.example.eventapp.R;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

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
public class EditEventFragment extends BottomSheetDialogFragment {
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
     * @param event The event that is going to be editedF
     * @param listener A listener to transfer the data to the parent fragment
     */
    public EditEventFragment(Event event, EditEventListener listener) {
        this.event = event;
        this.editEventListener = listener;
        this.oldPosterUri = event.getPosterUri();
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
        View view = inflater.inflate(R.layout.edit_event_popup, container, false);

        EditText eventName = view.findViewById(R.id.popup_edit_event_name);
        EditText eventDescription = view.findViewById(R.id.popup_edit_event_description);
        EditText maxEventEntrants = view.findViewById(R.id.popup_edit_event_max_entrants);
        FloatingActionButton saveEventButton = view.findViewById(R.id.popup_save_event_button);
        Button eventRegistrationDeadlineButton = view.findViewById(R.id.popup_edit_event_registration_deadline_button);
        CardView selectPosterButton = view.findViewById(R.id.popup_edit_event_add_poster_card);
        Button deleteEventButton = view.findViewById(R.id.popup_edit_event_delete_event_button);
        posterImageView = view.findViewById(R.id.popup_edit_event_image);

        // Initialize with current event data
        eventName.setText(event.getEventName());
        eventDescription.setText(event.getEventDescription());
        maxEventEntrants.setText(event.getMaxEntrants() == -1 ? "" : String.valueOf(event.getMaxEntrants()));
        if (event.hasPoster()) {
            Glide.with(this).load(event.getPosterUri()).into(posterImageView);
        } else {
            posterImageView.setImageBitmap(PhotoManager.generateDefaultPoster(event.getDocumentId()));
        }

        eventRegistrationDeadlineButton.setOnClickListener(v -> runDatePickers());

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
            if (saveEventButton.isFocusable()) {
                String newName = eventName.getText().toString();
                String newDescription = eventDescription.getText().toString();
                String maxEntrants = maxEventEntrants.getText().toString();

                if (newName.isEmpty()) {
                    Toast.makeText(getContext(), "Event name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveEventButton.setFocusable(false);

                // Update event details
                event.setEventName(newName);
                event.setEventDescription(newDescription);
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
                            saveEventButton.setFocusable(true);
                            Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                        }
                    };

                    if (oldPosterUri == null) {
                        PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "events", "poster", uploadCallback);
                    } else {
                        if (Objects.requireNonNull(oldPosterUri.getLastPathSegment()).split("/").length > 1) {
                            final String id = Objects.requireNonNull(oldPosterUri.getLastPathSegment()).split("/")[1];
                            PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "events", id, "poster", uploadCallback);
                        }

                    }
                } else {
                    editEventListener.saveEditedEvent(event);
                }
            }
        });

        return view;
    }

    /**
     * Runs the date pickers, and then returns the timestamps of the dates selected
     * @return Start time, end time, and deadline of picked dates.
     */
    private void runDatePickers() {

        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Pair<Long, Long>> durationDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Registration Duration")
                .setCalendarConstraints(constraints.build())
                .build();

        AtomicReference<Pair<Long, Long>> durationDates = new AtomicReference<>();

        durationDatePicker.show(requireActivity().getSupportFragmentManager(), TAG);
        durationDatePicker.addOnPositiveButtonClickListener(dates -> {
            durationDates.set(dates);
            CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(durationDates.get().first);
            CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(durationDates.get().second);

            ArrayList<CalendarConstraints.DateValidator> listValidators =
                    new ArrayList<CalendarConstraints.DateValidator>();
            listValidators.add(dateValidatorMin);
            listValidators.add(dateValidatorMax);
            CalendarConstraints.Builder constraintsDeadline = new CalendarConstraints.Builder()
                    .setValidator(CompositeDateValidator.allOf(listValidators));

            MaterialDatePicker<Long> deadlineDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Deadline")
                    .setCalendarConstraints(constraintsDeadline.build())
                    .build();

            deadlineDatePicker.show(requireActivity().getSupportFragmentManager(), TAG);
            deadlineDatePicker.addOnPositiveButtonClickListener(date -> {
                eventsViewModel.setCreatingEventDatesInitialized(true);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(durationDates.get().first + 86400000);
                event.setStartDate(calendar.getTimeInMillis());
                String startTime = format.format(calendar.getTime());
                calendar.setTimeInMillis(durationDates.get().second + 86400000);
                String endTime = format.format(calendar.getTime());
                event.setEndDate(calendar.getTimeInMillis());
                calendar.setTimeInMillis(date + 86400000);
                String deadLine = format.format(calendar.getTime());
                event.setDeadline(calendar.getTimeInMillis());
            });
        });
    }
}
