package com.example.eventapp.ui.scanqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.databinding.FragmentScanQrBinding;
import com.example.eventapp.viewmodels.ScanQrViewModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanQrFragment extends Fragment {

    private FragmentScanQrBinding binding;

    // Define the launcher for scanning
    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    // Display the scanned result in the TextView
                    binding.textScanQr.setText("Scanned: " + result.getContents());
                } else {
                    binding.textScanQr.setText("Scan Cancelled");
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ScanQrViewModel scanQrViewModel =
                new ViewModelProvider(this).get(ScanQrViewModel.class);

        binding = FragmentScanQrBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up the scan button click listener
        binding.buttonScanQr.setOnClickListener(v -> initiateQrScan());

        return root;

//        final TextView textView = binding.textScanQr;
//        scanQrViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
//        return root;
    }

    private void initiateQrScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);  // Restrict to QR codes only

        scanLauncher.launch(options);  // Launch the scan
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}