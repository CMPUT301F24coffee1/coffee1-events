package com.example.eventapp.ui.profiles;

import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.databinding.ProfileInfoPopupBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.photos.PhotoManager;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class ProfileInfoFragment extends BottomSheetDialogFragment {

    private ProfileInfoPopupBinding binding;

    /**
     * Creates and populates the profile info fragment, populating it with the selected profile
     * information from the ViewModel
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Root of the fragment
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = ProfileInfoPopupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ProfileViewModel profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::updateUserInfo);
        return root;
    }

    /**
     * Sets the listener for the button to edit fragment
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NavController navController = NavHostFragment.findNavController(this);

        Button fragmentButton = view.findViewById(R.id.profile_info_manage_facilities);

        fragmentButton.setOnClickListener((v) -> {
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            navController.navigate(R.id.navigation_facilities);
        });

        FloatingActionButton editButton = view.findViewById(R.id.profile_edit_button);

        editButton.setOnClickListener((v) -> {
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            navController.navigate(R.id.navigation_profile_edit);
        });
    }

    /**
     * Updates the user information in the fragment's View
     * @param user The user pulled from the View Model
     */
    private void updateUserInfo(User user) {
        final TextView nameField = binding.profileInfoName;
        final TextView emailField = binding.profileInfoEmail;
        final TextView phoneField = binding.profileInfoPhone;
        final TextView idField = binding.profileInfoId;
        final ConstraintLayout manageFacilitiesContainer = binding.profileInfoManageFacilitiesContainer;
        final ImageView photo = binding.profileInfoPhoto;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        // Below only parses correct phone numbers
        phoneField.setText(PhoneNumberUtils.formatNumber(user.getPhoneNumber(), Locale.getDefault().getCountry()));
        idField.setText(user.getUserId());
        manageFacilitiesContainer.setVisibility(user.isOrganizer() ? View.VISIBLE : View.GONE);
        Uri photoUri = user.getPhotoUri();
        if (user.hasPhoto()) {
            Glide.with(requireContext())
                    .load(photoUri)
                    .into(photo);
        } else {
            photo.setImageBitmap(PhotoManager.generateDefaultProfilePicture(nameField.getText().toString(), user.getUserId()));
        }
    }

    /**
     * Makes sure to clear the binding
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
