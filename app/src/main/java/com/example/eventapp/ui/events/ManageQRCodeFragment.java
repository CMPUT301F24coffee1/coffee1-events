package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ManageQRCodeFragment extends BottomSheetDialogFragment {
    private final Event event;

    public ManageQRCodeFragment(Event event) {
        this.event = event;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_manage_qr_code, null);
        ImageView qrCodeImage = view.findViewById(R.id.fragment_manage_qr_code_qr_code_image);

        // set image to the qr code image


        return view;
    }

}
