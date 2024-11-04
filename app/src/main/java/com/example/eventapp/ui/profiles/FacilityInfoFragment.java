package com.example.eventapp.ui.profiles;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.models.Facility;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FacilityInfoFragment extends BottomSheetDialogFragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.facility_info_popup, null);

        ProfileViewModel profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        Facility facility = profileViewModel.getSelectedFacility();

        TextView facilityName = view.findViewById(R.id.facility_name);
        TextView facilityDesc = view.findViewById(R.id.facility_desc);

        facilityName.setText(facility.getFacilityName());
        facilityDesc.setText(facility.getFacilityDescription());

        return view;
    }

}
