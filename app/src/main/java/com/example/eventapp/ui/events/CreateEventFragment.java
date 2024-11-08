package com.example.eventapp.ui.events;
import static java.util.Arrays.asList;

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

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class CreateEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {
    private CreateEventListener createEventListener;
    private String posterUriString = "";
    private Uri selectedPhotoUri;
    private ImageView posterImageView;
    private ArrayList<Long> timestamps;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    interface CreateEventListener{
        void createEvent(Event event);
    }

    public CreateEventFragment(CreateEventListener createEventListener){
        this.createEventListener = createEventListener;
    }

    @Override
    public void setDate(long timestamp, int type){
        switch(type) {
            case 0: //startTimestamp
                timestamps.set(0, timestamp);
                break;
            case 1: //endTimestamp
                timestamps.set(1, timestamp);
                break;
            case 2: //deadlineTimestamp
                timestamps.set(2, timestamp);
                break;
        }
        Log.d("CreateEventsFragment", "timestamp set to: "+timestamp);
        Log.d("CreateEventsFragment", "type: "+type);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        timestamps = new ArrayList<>(asList(0L,0L,0L)); // (startTimestamp, endTimestamp, deadlineTimestamp)
        View view = inflater.inflate(R.layout.create_event_popup, null);
        EditText eventName = view.findViewById(R.id.popup_create_event_name);
        EditText eventDescription = view.findViewById(R.id.popup_create_event_description);
        CheckBox geolocationRequired = view.findViewById(R.id.popup_create_event_geolocation_checkbox);
        Button createEventButton = view.findViewById(R.id.popup_create_event_button);
        EditText maxEventEntrants = view.findViewById(R.id.popup_create_event_max_entrants);
        Button eventDurationButton = view.findViewById(R.id.popup_create_event_duration_button);
        Button eventRegistrationDeadlineButton = view.findViewById(R.id.popup_create_event_registration_deadline_button);
        Button selectPosterButton = view.findViewById(R.id.popup_create_event_add_poster);
        ImageView posterImageView = view.findViewById(R.id.popup_create_event_image);

        posterImageView.setImageResource(R.drawable.default_event_poster);
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

        eventDurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "set event duration button clicked");
                // closes and opens instantly, add/override animations later
                showDatePickerFragment(1);
                showDatePickerFragment(0);
            }
        });

        eventRegistrationDeadlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "set event deadline button clicked");
                showDatePickerFragment(2);
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("CreateEventFragment", "Create Button Clicked");
                String newEventName = eventName.getText().toString();
                String newEventDescription = eventDescription.getText().toString();
                String maxEntrants = maxEventEntrants.getText().toString();

                // Check if there is an event name
                if(newEventName.isEmpty()){
                    Toast.makeText(getContext(), "Must have event name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if any timestamps are zero
                if (timestamps.get(0) == 0 || timestamps.get(1) == 0 || timestamps.get(2) == 0) {
                    Toast.makeText(getContext(), "All event dates must be set", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedPhotoUri == null) {
                    // No photo selected, create the event directly
                    if (maxEntrants.isEmpty()) {
                        // No max entrant count given
                        createEventListener.createEvent(new Event(newEventName, "", newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                    } else {
                        try {
                            int max = Integer.parseInt(maxEntrants);
                            if (max > 0) {
                                createEventListener.createEvent(new Event(newEventName, "", newEventDescription, geolocationRequired.isChecked(), max, timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                            } else {
                                createEventListener.createEvent(new Event(newEventName, "", newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                            }
                        } catch (Exception e) {
                            // Could not parse input, create event without max entrants
                            createEventListener.createEvent(new Event(newEventName, "", newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                        }
                    }
                } else {
                    // Upload photo to Firebase storage and only create the event after a successful upload
                    PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "events", "poster", new PhotoManager.UploadCallback() {
                        @Override
                        public void onUploadSuccess(String downloadUrl) {
                            posterUriString = downloadUrl;
                            Log.d("PhotoUploader", "Photo uploaded successfully: " + posterUriString);

                            // After the photo is uploaded successfully, create the event
                            if (maxEntrants.isEmpty()) {
                                // No max entrant count given
                                createEventListener.createEvent(new Event(newEventName, posterUriString, newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                            } else {
                                try {
                                    int max = Integer.parseInt(maxEntrants);
                                    if (max > 0) {
                                        createEventListener.createEvent(new Event(newEventName, posterUriString, newEventDescription, geolocationRequired.isChecked(), max, timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                                    } else {
                                        createEventListener.createEvent(new Event(newEventName, posterUriString, newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                                    }
                                } catch (Exception e) {
                                    // Could not parse input, create event without max entrants
                                    createEventListener.createEvent(new Event(newEventName, posterUriString, newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2)));
                                }
                            }
                        }

                        @Override
                        public void onUploadFailure(Exception e) {
                            Log.e("PhotoUploader", "Upload failed", e);
                            Toast.makeText(getContext(), "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


        });
        return view;
    }

    private void showDatePickerFragment(int type){
        DatePickerFragment datePickerFragment = new DatePickerFragment(this, type);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
