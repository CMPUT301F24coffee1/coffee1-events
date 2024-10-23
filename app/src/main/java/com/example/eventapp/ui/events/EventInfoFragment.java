package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        eventName.setText("placeholder event name");

        return view;
    }
}
