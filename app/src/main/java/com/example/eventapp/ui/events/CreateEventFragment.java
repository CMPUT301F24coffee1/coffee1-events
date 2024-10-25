package com.example.eventapp.ui.events;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateEventFragment extends BottomSheetDialogFragment implements DatePickerFragment.SetDateListener {
    private CreateEventListener createEventListener;
    private long startTimeStamp;

    interface CreateEventListener{
        void createEvent(Event event);
    }

    public CreateEventFragment(CreateEventListener createEventListener){
        this.createEventListener = createEventListener;
    }

    @Override
    public void setDate(long timestamp){
        //just start time for now
        startTimeStamp = timestamp;
        Log.d("CreateEventsFragment", "timestamp set to: "+startTimeStamp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        startTimeStamp = 0;
        View view = inflater.inflate(R.layout.create_event_popup, null);
        EditText eventName = view.findViewById(R.id.popup_create_event_name);
        EditText eventDescription = view.findViewById(R.id.popup_create_event_description);
        CheckBox geolocationRequired = view.findViewById(R.id.popup_create_event_geolocation_checkbox);
        Button createEventButton = view.findViewById(R.id.popup_create_event_button);
        EditText maxEventEntrants = view.findViewById(R.id.popup_create_event_max_entrants);
        Button eventDurationButton = view.findViewById(R.id.popup_create_event_duration_button);

        eventDurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "set event duration button clicked");
                showDatePickerFragment();
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CreateEventFragment", "Create Button Clicked");
                String newEventName = eventName.getText().toString();
                String newEventDescription = eventDescription.getText().toString();
                String maxEntrants = maxEventEntrants.getText().toString();
                if(maxEntrants.equals("")){
                    //no max entrant count given
                    createEventListener.createEvent(new Event(newEventName, newEventDescription,geolocationRequired.isChecked()));
                }else{
                    try{
                        int max = Integer.parseInt(maxEntrants);
                        if(max>0){
                            createEventListener.createEvent(new Event(newEventName, newEventDescription,geolocationRequired.isChecked(), max));
                        }else{
                            createEventListener.createEvent(new Event(newEventName, newEventDescription,geolocationRequired.isChecked()));
                        }
                    }catch (Exception e){
                        //could not parse input
                        createEventListener.createEvent(new Event(newEventName, newEventDescription,geolocationRequired.isChecked()));
                    }
                }

            }
        });
        return view;
    }

    private void showDatePickerFragment(){
        DatePickerFragment datePickerFragment = new DatePickerFragment(this);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
