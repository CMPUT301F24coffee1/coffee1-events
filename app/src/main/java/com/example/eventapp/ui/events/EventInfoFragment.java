package com.example.eventapp.ui.events;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Date;

public class EventInfoFragment extends BottomSheetDialogFragment {

    private final Event event;

    public EventInfoFragment (Event event) {
        this.event = event;
    }

    interface EditEventInfoListener{
        void editEventInfo(Event event);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.event_info_popup, null);
        TextView eventName = view.findViewById(R.id.popup_event_name_text);
        Button editEventButton = view.findViewById(R.id.popup_edit_event_info_button);
        ImageView eventImage = view.findViewById(R.id.popup_event_poster_image);
        TextView eventDuration = view.findViewById(R.id.popup_event_duration_text);
        TextView eventRegistrationDeadline = view.findViewById(R.id.popup_event_registration_deadline_text);
        TextView eventDescription = view.findViewById(R.id.popup_event_description_text);
        TextView eventEntrantsCount = view.findViewById(R.id.popup_create_event_max_entrants);

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

        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("EventInfoFragment", "Edit Button Clicked");
            }
        });
        return view;
    }
}
