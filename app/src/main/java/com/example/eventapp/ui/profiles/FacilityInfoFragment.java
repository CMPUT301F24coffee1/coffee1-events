package com.example.eventapp.ui.profiles;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Facility;
import com.example.eventapp.ui.images.ImageInfoFragment;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * FacilityInfoFragment displays detailed information about a selected facility in a bottom sheet dialog.
 * This fragment retrieves the selected facility's information from a shared ProfileViewModel, displaying
 * attributes such as the facility name, description, and photo. The fragment also provides an edit button
 * allowing users to navigate to the facility edit screen.
 *
 * <p>This fragment is used as a part of the user interface to present facility details in a focused view.
 * It leverages Glide for image loading and uses Android Navigation for transitions.</p>
 */
public class FacilityInfoFragment extends BottomSheetDialogFragment {

    /**
     * Creates and populates the facility info fragment, populating it with the selected facility
     * information from the ViewModel
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view that was just populated
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.facility_info_popup, null);

        ProfileViewModel profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        Facility facility = profileViewModel.getSelectedFacility();

        TextView facilityName = view.findViewById(R.id.facility_name);
        TextView facilityDesc = view.findViewById(R.id.facility_desc);
        CardView facilityPhotoCard = view.findViewById(R.id.facility_photo_card);
        ImageView facilityPhoto = view.findViewById(R.id.facility_photo);

        facilityName.setText(facility.getFacilityName());
        facilityDesc.setText(facility.getFacilityDescription());

        if (facility.hasPhoto()) {
            Glide.with(requireContext())
                    .load(facility.getPhotoUri())
                    .into(facilityPhoto);
            facilityPhotoCard.setOnClickListener((v) -> {
                if (v.isClickable()) {
                    ImagesViewModel imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
                    imagesViewModel.setSelectedImage(facility.getPhotoUri());
                    new ImageInfoFragment().show(requireActivity().getSupportFragmentManager(), "fragment_image_info");
                }
            });
            facilityPhotoCard.setClickable(true);
            facilityPhotoCard.setFocusable(true);
        } else {
            facilityPhotoCard.setClickable(false);
            facilityPhotoCard.setFocusable(false);
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
