package com.example.eventapp.ui.scanqr;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.databinding.FragmentScanQrBinding;
import com.example.eventapp.models.Event;
import com.example.eventapp.ui.events.ScannedEventFragment;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.example.eventapp.viewmodels.ScanQrViewModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class ScanQrFragment extends Fragment {

    private FragmentScanQrBinding binding;
    private ScanQrViewModel scanQrViewModel;
    private EventsViewModel eventsViewModel;
    private ScannedEventFragment currentScannedEventFragment;

    // Define the launcher for scanning
    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedData = result.getContents();
                    scanQrViewModel.setText(scannedData); // Update ViewModel with scanned data
                    // Display the scanned result in the TextView
                    binding.textScanQr.setText("Scanned: " + result.getContents());

                    eventsViewModel.getEventByQrCodeHash(result.getContents())
                        .thenAccept(scannedEvent -> {
                            Log.d(TAG, result.getContents());
                            Log.d(TAG, String.valueOf(scannedEvent));
                            currentScannedEventFragment = new ScannedEventFragment(scannedEvent);
                            currentScannedEventFragment.show(requireActivity().getSupportFragmentManager(), "scanned_event_info");
                        })
                        .exceptionally(throwable -> {
                            Log.e(TAG, "Failed to scan event with id: " + result.getContents(), throwable);
                            return null;
                        });

                } else { // Else toast "Scan cancelled"
                    Toast.makeText(getContext(), "Scan Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scanQrViewModel = new ViewModelProvider(this).get(ScanQrViewModel.class);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);

        binding = FragmentScanQrBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonScanQr.setOnClickListener(v -> initiateQrScan());

        return root;
    }



    private void initiateQrScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setOrientationLocked(false);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);  // Restrict to QR codes only

        scanLauncher.launch(options);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}