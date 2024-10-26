package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EventInfoFragment extends BottomSheetDialogFragment {
    interface EditEventInfoListener{
        void editEventInfo(Event event);
    }

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

        // placeholders
        eventName.setText("PLACEHOLDER NAME");
        eventDuration.setText("January 1st - December 1st");
        eventRegistrationDeadline.setText("Registration Deadline: January 1st");
        eventEntrantsCount.setText("Entrants: 80/90");

        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("EventInfoFragment", "Edit Button Clicked");
            }
        });
        return view;
    }
}
