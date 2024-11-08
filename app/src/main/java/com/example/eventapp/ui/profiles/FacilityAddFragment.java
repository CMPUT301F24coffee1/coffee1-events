package com.example.eventapp.ui.profiles;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentFacilityAddBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/**
 * FacilityAddFragment is a fragment responsible for providing the interface to add a new Facility.
 * It integrates with the ProfileViewModel to persist facility details and enables users to input
 * facility information including name, description, and an optional photo. This fragment includes
 * form validation, image uploading, and user feedback for successful facility creation.
 */
public class FacilityAddFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentFacilityAddBinding binding;
    private Facility facility;
    private Uri selectedPhotoUri;
    private String photoUriString = "";

    private enum Confirmed { YES, NAME }

    /**
     * Behaviour to run when the View is created, in this case,
     * creating the View Model, linking it, and using the View Model
     * to fill out user information for the facility
     * Also populates the menu with a confirm button, and specifies that the data should be
     * sent to the View Model on confirmation
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment.
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

        // Inflate the menu with the facility add button set
        NavController navController = NavHostFragment.findNavController(this);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_nav_menu_facility_add, menu);
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_facility_confirm_add) {
                    // Here because we only want this behaviour to happen when hitting confirm,
                    // not the back button
                    boolean isConfirmed = false;
                    FacilityAddFragment.Confirmed confirmable = confirmable();
                    final String error;
                    if (Objects.requireNonNull(confirmable) == Confirmed.NAME) {
                        error = getString(R.string.facility_name_cannot_be_empty);
                    } else {
                        error = ""; // No error
                        isConfirmed = true;
                    }
                    if (isConfirmed) {
                        if (selectedPhotoUri != null) {
                            // Upload photo to Firebase storage and only confirm if the upload is successful
                            PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "facilities", "photo", new PhotoManager.UploadCallback() {
                                @Override
                                public void onUploadSuccess(String downloadUrl) {
                                    photoUriString = downloadUrl;
                                    Log.d("PhotoUploader", "Photo uploaded successfully: " + photoUriString);
                                    addFacility();
                                    navController.popBackStack();
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    Log.e("PhotoUploader", "Upload failed", e);
                                    Toast.makeText(getContext(), getString(R.string.photo_upload_failed), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // If photo wasn't changed, nothing needs to be uploaded,
                            // so we can just add the facility as is
                            addFacility();
                            navController.popBackStack();
                        }
                    } else {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        binding = FragmentFacilityAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileViewModel =
                new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Since there is no facility, it will make a blank one
        facility = profileViewModel.newSelectedFacility();

        final EditText nameField = binding.facilityAddNameInput;
        final EditText descField = binding.facilityAddDescInput;
        final ImageView photo = binding.facilityAddPhoto;
        final FloatingActionButton removePhoto = binding.facilityAddRemovePhoto;

        nameField.setText(facility.getFacilityName());
        descField.setText(facility.getFacilityDescription());

        if (facility.hasPhoto()) {
            removePhoto.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(facility.getPhotoUri())
                    .into(photo);
        } else {
            removePhoto.setVisibility(View.GONE);
            photo.setImageResource(R.drawable.ic_facility_24dp);
        }

        removePhoto.setOnClickListener(v -> {
            removePhoto.setVisibility(View.GONE);
            photo.setImageResource(R.drawable.ic_facility_24dp);
            selectedPhotoUri = null;
        });

        return root;
    }

    /**
     * Sets the listener for the button to add/edit photo
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ImageView photo = binding.facilityAddPhoto;
        final CardView photoCard = binding.facilityAddPhotoCard;
        final FloatingActionButton removePhoto = binding.facilityAddRemovePhoto;

        PhotoPicker.PhotoPickerCallback pickerCallback = photoUri -> {
            // Save the URI for later use after validation
            selectedPhotoUri = photoUri;
            Glide.with(requireView()).load(selectedPhotoUri).into(photo);
            removePhoto.setVisibility(View.VISIBLE);
        };

        ActivityResultLauncher<Intent> photoPickerLauncher = PhotoPicker.getPhotoPickerLauncher(this, pickerCallback);

        photoCard.setOnClickListener(v -> PhotoPicker.openPhotoPicker(photoPickerLauncher));
    }

    /**
     * Checks if the form is valid to be possible to confirm
     */
    private FacilityAddFragment.Confirmed confirmable() {
        final EditText nameField = binding.facilityAddNameInput;
        final String name = Objects.requireNonNull(nameField.getText()).toString();

        if (name.isEmpty())
            return FacilityAddFragment.Confirmed.NAME;

        return FacilityAddFragment.Confirmed.YES;
    }

    /**
     * Adds the facility to the viewmodel
     */
    private void addFacility() {
        // do confirm routine
        try {
            final EditText nameField = binding.facilityAddNameInput;
            final EditText descField = binding.facilityAddDescInput;

            facility.setFacilityName(nameField.getText().toString());
            facility.setFacilityDescription(descField.getText().toString());
            facility.setPhotoUriString(photoUriString);

            profileViewModel.addFacility(facility);
        } catch (Exception e) {
            Log.e(TAG, "Failure to add facility: ", e);
            Toast.makeText(getContext(), R.string.facility_creation_failed, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Clears the binding
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
