package com.example.eventapp.ui.profiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.databinding.FragmentAdminProfilesBinding;

public class AdminProfilesFragment extends Fragment {

    private FragmentAdminProfilesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AdminProfilesViewModel adminProfilesViewModel =
                new ViewModelProvider(this).get(AdminProfilesViewModel.class);

        binding = FragmentAdminProfilesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textProfiles;
        adminProfilesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}