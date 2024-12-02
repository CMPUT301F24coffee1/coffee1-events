package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.QRCodeGenerator;
import com.example.eventapp.viewmodels.EntrantsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ManageQRCodeFragment extends BottomSheetDialogFragment {
    private Event event;
    private EntrantsViewModel entrantsViewModel;

    public ManageQRCodeFragment(Event event) {
        this.event = event;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_manage_qr_code, null);
        ImageView qrCodeImage = view.findViewById(R.id.fragment_manage_qr_code_qr_code_image);
        entrantsViewModel = new ViewModelProvider(requireActivity()).get(EntrantsViewModel.class);
        event = entrantsViewModel.getCurrentEventToQuery();

        // set image to the qr code image
        QRCodeGenerator qrCodeGenerator = new QRCodeGenerator(event.getQrCodeHash(), 400, 400);
        qrCodeGenerator.generateQRCodeBitmap();
        qrCodeImage.setImageBitmap(qrCodeGenerator.getQrCodeBitmap());

        // set save button
        Button saveButton = view.findViewById(R.id.fragment_manage_qr_code_save_button);
        saveButton.setOnClickListener(view1 -> {
            // save bitmap image
            Log.d("ManageQRCodeFragment", "Save QRCode Bitmap");
        });
        saveButton.setOnClickListener(view1 -> {
            // save bitmap image
            Log.d("ManageQRCodeFragment", "Save QRCode Bitmap");
        });

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                if (user.isAdmin()) {
                    Button deleteButton = view.findViewById(R.id.fragment_manage_qr_code_delete_button);
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(view13 -> {
                        // delete qrcode hash from database

                    });

                    Button generateButton = view.findViewById(R.id.fragment_manage_qr_code_generate_button);
                    generateButton.setVisibility(View.VISIBLE);
                    generateButton.setOnClickListener(view12 -> {
                        qrCodeGenerator.generateQRCodeBitmap();
                        // re-add qrcode hash to database

                        qrCodeImage.setImageBitmap(qrCodeGenerator.getQrCodeBitmap());
                    });

                }
            }
        });

        return view;
    }

}
