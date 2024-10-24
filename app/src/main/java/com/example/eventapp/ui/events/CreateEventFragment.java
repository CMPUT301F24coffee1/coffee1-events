package com.example.eventapp.ui.events;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateEventFragment extends BottomSheetDialogFragment {
    private CreateEventListener createEventListener;

    interface CreateEventListener{
        void createEvent(Event event);
    }

    public CreateEventFragment(CreateEventListener createEventListener){
        this.createEventListener = createEventListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.create_event_popup, null);
        EditText eventName = view.findViewById(R.id.popup_create_event_name);
        EditText eventDescription = view.findViewById(R.id.popup_create_event_description);

        Button createEventButton = view.findViewById(R.id.popup_create_event_button);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "Create Button Clicked");
                String newEventName = eventName.getText().toString();
                String newEventDescription = eventDescription.getText().toString();
                // placeholder event for now, replace later
                createEventListener.createEvent(new Event(newEventName, newEventDescription));
            }
        });
        return view;
    }
}
