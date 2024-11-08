package com.example.eventapp.ui.profiles;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
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
import com.example.eventapp.databinding.FragmentFacilityEditBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class FacilityEditFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentFacilityEditBinding binding;
    private Facility facility;
    private Uri selectedPhotoUri;
    private Uri oldPhotoUri;
    private String photoUriString = "";
    private boolean removingPhoto = false;

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

        // Inflate the menu with the facility edit button set
        NavController navController = NavHostFragment.findNavController(this);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_nav_menu_facility_edit, menu);
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_facility_confirm) {
                    // Here because we only want this behaviour to happen when hitting confirm,
                    // not the back button
                    boolean isConfirmed = false;
                    FacilityEditFragment.Confirmed confirmable = confirmable();
                    final String error; // Array for finality in lambda statement
                    if (Objects.requireNonNull(confirmable) == Confirmed.NAME) {
                        error = getString(R.string.facility_name_cannot_be_empty);
                    } else {
                        error = ""; // No error
                        isConfirmed = true;
                    }
                    if (isConfirmed) {
                        if (selectedPhotoUri != null) {
                            // Upload photo to Firebase storage and only confirm if the upload is successful
                            PhotoManager.UploadCallback uploadCallback = new PhotoManager.UploadCallback() {
                                @Override
                                public void onUploadSuccess(String downloadUrl) {
                                    photoUriString = downloadUrl;
                                    Log.d("PhotoUploader", "Photo uploaded successfully: " + photoUriString);
                                    updateFacility();
                                    navController.popBackStack();
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    Log.e("PhotoUploader", "Upload failed", e);
                                    Toast.makeText(getContext(), getString(R.string.photo_upload_failed), Toast.LENGTH_SHORT).show();
                                }
                            };
                            if (oldPhotoUri == null) {
                                PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "facilities","photo", uploadCallback);
                            } else {
                                // Overwrite old photo
                                final String id = Objects.requireNonNull(oldPhotoUri.getLastPathSegment()).split("/")[1];
                                PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "facilities","photo", id, uploadCallback);
                            }

                        } else {
                            // If photo wasn't changed, nothing needs to be uploaded,
                            // so we can just add the facility as is
                            if (oldPhotoUri != null) {
                                if (removingPhoto) {
                                    profileViewModel.removePhotoOfSelectedFacility();
                                } else {
                                    photoUriString = oldPhotoUri.toString();
                                }
                            }
                            updateFacility();
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

        binding = FragmentFacilityEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileViewModel =
                new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        facility = profileViewModel.getSelectedFacility();

        final EditText nameField = binding.facilityEditNameInput;
        final EditText descField = binding.facilityEditDescInput;
        final ImageView photo = binding.facilityEditPhoto;
        final FloatingActionButton removePhoto = binding.facilityEditRemovePhoto;

        nameField.setText(facility.getFacilityName());
        descField.setText(facility.getFacilityDescription());

        if (facility.hasPhoto()) {
            removePhoto.setVisibility(View.VISIBLE);
            oldPhotoUri = facility.getPhotoUri();
            Glide.with(requireContext())
                    .load(facility.getPhotoUri())
                    .into(photo);
        } else {
            removePhoto.setVisibility(View.GONE);
            photo.setImageResource(R.drawable.ic_facility_24dp);
        }

        return root;
    }

    /**
     * Sets the listener for the button to delete fragment, and button to add/edit photo
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ImageView photo = binding.facilityEditPhoto;
        final CardView photoCard = binding.facilityEditPhotoCard;
        final FloatingActionButton removePhoto = binding.facilityEditRemovePhoto;

        PhotoPicker.PhotoPickerCallback pickerCallback = photoUri -> {
            // Save the URI for later use after validation
            selectedPhotoUri = photoUri;
            Glide.with(requireView()).load(selectedPhotoUri).into(photo);
            removingPhoto = false;
            removePhoto.setVisibility(View.VISIBLE);
        };

        ActivityResultLauncher<Intent> photoPickerLauncher = PhotoPicker.getPhotoPickerLauncher(this, pickerCallback);

        photoCard.setOnClickListener(v -> PhotoPicker.openPhotoPicker(photoPickerLauncher));

        NavController navController = NavHostFragment.findNavController(this);

        FloatingActionButton deleteButton = view.findViewById(R.id.facility_edit_delete);

        deleteButton.setOnClickListener((v) -> new AlertDialog.Builder(getActivity())
            .setTitle(R.string.warning)
            .setMessage(R.string.confirm_delete_facility)
            .setPositiveButton(R.string.confirm, (dialog, id) -> {
                profileViewModel.removeSelectedFacility();
                navController.popBackStack();
            }).setNegativeButton(R.string.cancel, (dialog, id) -> {
            }).create().show());

        removePhoto.setOnClickListener(v -> {
            removingPhoto = true;
            removePhoto.setVisibility(View.GONE);
            photo.setImageResource(R.drawable.ic_facility_24dp);
            selectedPhotoUri = null;
        });
    }

    /**
     * Checks if the edit form is valid to be possible to confirm
     */
    private FacilityEditFragment.Confirmed confirmable() {
        final EditText nameField = binding.facilityEditNameInput;
        final String name = Objects.requireNonNull(nameField.getText()).toString();

        if (name.isEmpty())
            return FacilityEditFragment.Confirmed.NAME;

        return FacilityEditFragment.Confirmed.YES;
    }

    /**
     * Updates the facility and sends it to the ViewModel
     */
    private void updateFacility() {
        try {
            // do confirm routine
            final EditText nameField = binding.facilityEditNameInput;
            final EditText descField = binding.facilityEditDescInput;

            facility.setFacilityName(nameField.getText().toString());
            facility.setFacilityDescription(descField.getText().toString());
            facility.setPhotoUriString(photoUriString);

            profileViewModel.updateSelectedFacility(facility);
        }  catch (Exception e) {
            Log.e(TAG, "Failure to update facility: ", e);
            Toast.makeText(getContext(), R.string.facility_update_failed, Toast.LENGTH_SHORT).show();
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
