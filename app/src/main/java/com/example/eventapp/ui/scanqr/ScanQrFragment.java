package com.example.eventapp.ui.scanqr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.databinding.FragmentScanQrBinding;
import com.example.eventapp.viewmodels.ScanQrViewModel;

public class ScanQrFragment extends Fragment {

    private FragmentScanQrBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ScanQrViewModel scanQrViewModel =
                new ViewModelProvider(this).get(ScanQrViewModel.class);

        binding = FragmentScanQrBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textScanQr;
        scanQrViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}