package com.example.eventapp.ui.events;
import static java.util.Arrays.asList;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResultLauncher;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.photos.PhotoPickerUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;

public class CreateEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {
    private CreateEventListener createEventListener;
    private Uri posterUri;
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
        posterImageView = view.findViewById(R.id.popup_create_event_image);

        // Initialize photo picker launcher
        photoPickerLauncher = PhotoPickerUtils.getPhotoPickerLauncher(this, new PhotoPickerUtils.PhotoPickerCallback() {
            @Override
            public void onPhotoPicked(Uri photoUri) {
                posterUri = photoUri;
                posterImageView.setImageURI(photoUri); // Display the selected image
            }
        });

        // Set listeners
        selectPosterButton.setOnClickListener(v -> PhotoPickerUtils.openPhotoPicker(photoPickerLauncher));

        eventDurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "set event duration button clicked");
                // closes and opens instantly, add/override animations later
                showDatePickerFragment(0);
                showDatePickerFragment(1);
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

                // referenced zxing-android-embedded's "Generate Barcode" example (https://github.com/journeyapps/zxing-android-embedded)

                if(maxEntrants.equals("")){
                    // no max entrant count given
                    createEventListener.createEvent(new Event(newEventName, posterUri, newEventDescription, geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2), qrCodeBitmap));
                }else{
                    try{
                        int max = Integer.parseInt(maxEntrants);
                        if(max>0){
                            createEventListener.createEvent(new Event(newEventName, posterUri, newEventDescription,geolocationRequired.isChecked(), max, timestamps.get(0), timestamps.get(1), timestamps.get(2), qrCodeBitmap));
                        }else{
                            createEventListener.createEvent(new Event(newEventName, posterUri, newEventDescription,geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2), qrCodeBitmap));
                        }
                    }catch (Exception e){
                        // could not parse input
                        createEventListener.createEvent(new Event(newEventName, posterUri, newEventDescription,geolocationRequired.isChecked(), timestamps.get(0), timestamps.get(1), timestamps.get(2), qrCodeBitmap));
                    }
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
