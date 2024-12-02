package com.example.eventapp.ui.events;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.QRCodeGenerator;
import com.example.eventapp.viewmodels.EntrantsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

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
            Bitmap qrCodeBitmap = qrCodeGenerator.getQrCodeBitmap();
            if (qrCodeBitmap != null) {
                saveBitmap(qrCodeBitmap);
            } else {
                Toast.makeText(getContext(), "QR Code not available to save", Toast.LENGTH_SHORT).show();
            }
        });

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                if (user.isAdmin()) {
                    Button deleteButton = view.findViewById(R.id.fragment_manage_qr_code_delete_button);
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(view13 -> {
                        entrantsViewModel.deleteQrCodeHash()
                                .thenAccept(aVoid -> {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "QR code hash deleted", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .exceptionally(e -> {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Failed to delete QR code hash: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                    return null;
                                });
                    });

                    Button generateButton = view.findViewById(R.id.fragment_manage_qr_code_generate_button);
                    generateButton.setVisibility(View.VISIBLE);
                    generateButton.setOnClickListener(view12 -> {
                        qrCodeGenerator.generateQRCodeBitmap();
                        qrCodeImage.setImageBitmap(qrCodeGenerator.getQrCodeBitmap());

                        entrantsViewModel.reAddQrCodeHash()
                                .thenAccept(aVoid -> {
                                    requireActivity().runOnUiThread(() -> {
                                        qrCodeImage.setImageBitmap(qrCodeGenerator.getQrCodeBitmap());
                                        Toast.makeText(getContext(), "QR code hash updated", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .exceptionally(e -> {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Failed to update QR code hash: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                    return null;
                                });
                    });
                }
            }
        });
        return view;
    }
    private void saveBitmap(Bitmap bitmap) {
        ContentResolver resolver = requireContext().getContentResolver();
        String fileName = event.getEventName() + "-QR-Code.png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage (API 29+)
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppQRCodes");

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                    assert outputStream != null;
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(getContext(), "QR Code saved to Photos", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Failed to save QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Legacy storage (< API 29)
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File qrCodeDirectory = new File(directory, "MyAppQRCode");
            if (!qrCodeDirectory.exists()) {
                qrCodeDirectory.mkdirs();
            }

            File file = new File(qrCodeDirectory, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // Notify the media scanner to add the file to the Photos app
                MediaScannerConnection.scanFile(getContext(), new String[]{file.getAbsolutePath()}, null, null);
                Toast.makeText(getContext(), "QR Code saved to Photos", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to save QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
