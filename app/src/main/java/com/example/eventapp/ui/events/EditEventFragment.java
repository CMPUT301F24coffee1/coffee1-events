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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.bumptech.glide.Glide;
import com.example.eventapp.photos.PhotoPicker;
import com.example.eventapp.photos.PhotoUploader;
import com.example.eventapp.models.Event;
import com.example.eventapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class EditEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {

    private Event event;
    private String posterUriString;
    private String oldPosterUriString;
    private Uri selectedPhotoUri;
    private ImageView posterImageView;
    private ArrayList<Long> timestamps;
    private EditEventListener editEventListener;

    interface EditEventListener {
        void saveEditedEvent(Event event);
    }

    public EditEventFragment(Event event, EditEventListener listener) {
        this.event = event;
        this.editEventListener = listener;
        this.oldPosterUriString = event.getPosterUriString();
    }

    @Override
    public void setDate(long timestamp, int type) {
        switch(type) {
            case 0: timestamps.set(0, timestamp); break;
            case 1: timestamps.set(1, timestamp); break;
            case 2: timestamps.set(2, timestamp); break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            showDatePickerFragment(0);
            showDatePickerFragment(1);
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
                PhotoUploader.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, new PhotoUploader.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        event.setPosterUriString(downloadUrl);

                        // Delete the old poster from Firebase Storage if it exists
                        if (oldPosterUriString != null && !oldPosterUriString.isEmpty()) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference oldPosterRef = storage.getReferenceFromUrl(oldPosterUriString);
                            oldPosterRef.delete().addOnSuccessListener(aVoid -> {
                                Log.d("EditEventFragment", "Old poster successfully deleted from Firebase.");
                            }).addOnFailureListener(e -> {
                                Log.e("EditEventFragment", "Failed to delete old poster.", e);
                            });
                        }
                        editEventListener.saveEditedEvent(event);
                    }

                    @Override
                    public void onUploadFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                editEventListener.saveEditedEvent(event);
            }
        });

        return view;
    }

    private void showDatePickerFragment(int type) {
        DatePickerFragment datePickerFragment = new DatePickerFragment(this, type);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
