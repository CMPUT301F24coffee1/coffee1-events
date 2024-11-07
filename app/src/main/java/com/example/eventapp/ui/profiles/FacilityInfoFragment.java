package com.example.eventapp.ui.profiles;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Facility;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FacilityInfoFragment extends BottomSheetDialogFragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.facility_info_popup, null);

        ProfileViewModel profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        Facility facility = profileViewModel.getSelectedFacility();

        TextView facilityName = view.findViewById(R.id.facility_name);
        TextView facilityDesc = view.findViewById(R.id.facility_desc);
        ImageView facilityPhoto = view.findViewById(R.id.facility_photo);

        facilityName.setText(facility.getFacilityName());
        facilityDesc.setText(facility.getFacilityDescription());

        if (facility.hasPhoto()) {
            Glide.with(requireContext())
                    .load(facility.getPhotoUri())
                    .into(facilityPhoto);
        } else {
            facilityPhoto.setImageResource(R.drawable.ic_facility_24dp);
        }

        return view;
    }

    /**
     * Sets the listener for the button to edit fragment
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NavController navController = NavHostFragment.findNavController(this);

        FloatingActionButton editButton = view.findViewById(R.id.facility_edit_button);

        editButton.setOnClickListener((v) -> {
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            navController.navigate(R.id.navigation_facility_edit);
        });
    }

}
