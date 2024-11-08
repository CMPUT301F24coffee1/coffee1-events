package com.example.eventapp.ui.profiles;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.example.eventapp.databinding.FragmentProfileEditBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.services.photos.PhotoPicker;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileEditFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileEditBinding binding;
    private List<Facility> facilities;
    private Uri selectedPhotoUri;
    private Uri oldPhotoUri;
    private String photoUriString = "";
    private boolean removingPhoto = false;
    private String userId;
    private boolean userHasPhoto = false;

    private enum Confirmed { YES, NAME, EMAIL, PHONE, ORGANIZER }

    /**
     * Behaviour to run when the View is created, in this case,
     * creating the View Model, linking it, and using the View Model
     * to fill out user information for the profile
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

        facilities = new ArrayList<>();

        // Inflate the menu with the profile edit button set
        NavController navController = NavHostFragment.findNavController(this);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_nav_menu_profile_edit, menu);
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_profile_confirm) {
                    // Here because we only want this behaviour to happen when hitting confirm,
                    // not the back button
                    boolean isConfirmed = false;
                    Confirmed confirmable = confirmable();
                    final String[] error = new String[1]; // Array for finality in lambda statement
                    switch (confirmable) {
                        case NAME:
                            error[0] = getString(R.string.name_cannot_be_empty);
                            break;
                        case EMAIL:
                            error[0] = getString(R.string.email_format_incorrect);
                            break;
                        case PHONE:
                            error[0] = getString(R.string.phone_format_incorrect);
                            break;
                        case ORGANIZER:
                            // Possible delete all facilities functionality for later
//                            new AlertDialog.Builder(getActivity()).setMessage(R.string.confirm_delete_facilities)
//                            .setPositiveButton(R.string.confirm, (dialog, id) -> {
//                                isConfirmed = true;
//                            }).setNegativeButton(R.string.cancel, (dialog, id) -> {
//                                error[0] = getString(R.string.profile_update_cancelled);
//                            }).create();
                            error[0] = getString(R.string.has_facilities);
                            break;
                        default:
                            isConfirmed = true;
                            break;
                    }
                    if (isConfirmed) {
                        if (selectedPhotoUri != null) {
                            // Upload photo to Firebase storage and only confirm if the upload is successful
                            PhotoManager.UploadCallback uploadCallback = new PhotoManager.UploadCallback() {
                                @Override
                                public void onUploadSuccess(String downloadUrl) {
                                    photoUriString = downloadUrl;
                                    Log.d("PhotoUploader", "Photo uploaded successfully: " + photoUriString);
                                    updateUser();
                                    navController.popBackStack();
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    Log.e("PhotoUploader", "Upload failed", e);
                                    Toast.makeText(getContext(), getString(R.string.photo_upload_failed), Toast.LENGTH_SHORT).show();
                                }
                            };

                            if (oldPhotoUri == null) {
                                PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "profiles","photo", uploadCallback);
                            } else {
                                // Overwrite old photo
                                final String id = Objects.requireNonNull(oldPhotoUri.getLastPathSegment()).split("/")[1];
                                PhotoManager.uploadPhotoToFirebase(getContext(), selectedPhotoUri, 75, "profiles", id, "photo", uploadCallback);
                            }
                        } else {
                            // If photo wasn't changed, nothing needs to be uploaded,
                            // so we can just update the user as is
                            if (oldPhotoUri != null) {
                                if (removingPhoto) {
                                    profileViewModel.removeUserPhoto();
                                } else {
                                    photoUriString = oldPhotoUri.toString();
                                }
                            }
                            updateUser();
                            navController.popBackStack();
                        }
                    } else {
                        Toast.makeText(getContext(), error[0], Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        binding = FragmentProfileEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileViewModel =
                new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::updateUserInfo);
        profileViewModel.getFacilities().observe(getViewLifecycleOwner(), this::updateFacilities);
        profileViewModel.getActualUser().observe(getViewLifecycleOwner(), this::verifyDeleteButton);

        return root;
    }

    /**
     * Sets the listener for the button to edit photo
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ImageView photo = binding.profileEditPhoto;
        final CardView photoCard = binding.profileEditPhotoCard;
        final FloatingActionButton removePhoto = binding.profileEditRemovePhoto;
        final EditText nameField = binding.profileEditNameInput;
        final FloatingActionButton deleteButton = binding.profileEditDelete;

        PhotoPicker.PhotoPickerCallback pickerCallback = photoUri -> {
            // Save the URI for later use after validation
            selectedPhotoUri = photoUri;
            Glide.with(requireView()).load(selectedPhotoUri).into(photo);
            removingPhoto = false;
            removePhoto.setVisibility(View.VISIBLE);
        };

        ActivityResultLauncher<Intent> photoPickerLauncher = PhotoPicker.getPhotoPickerLauncher(this, pickerCallback);

        photoCard.setOnClickListener(v -> PhotoPicker.openPhotoPicker(photoPickerLauncher));

        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!userHasPhoto) {
                    photo.setImageBitmap(PhotoManager.generateDefaultProfilePicture(nameField.getText().toString(), userId));
                }
            }
        });

        removePhoto.setOnClickListener(v -> {
            userHasPhoto = false;
            removingPhoto = true;
            removePhoto.setVisibility(View.GONE);
            photo.setImageBitmap(PhotoManager.generateDefaultProfilePicture(nameField.getText().toString(), userId));
            selectedPhotoUri = null;
        });

        deleteButton.setOnClickListener(v -> {
            if (hasFacilities()) {
                Toast.makeText(getContext(), getString(R.string.error_cant_delete_profile), Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(getActivity()).setMessage(R.string.confirm_delete_profile)
                    .setPositiveButton(R.string.confirm, (dialog, id) -> {
                        profileViewModel.deleteSelectedUser();
                        NavHostFragment.findNavController(this).popBackStack();
                    }).setNegativeButton(R.string.cancel, (dialog, id) ->
                        Toast.makeText(getContext(), getString(R.string.profile_delete_cancelled), Toast.LENGTH_SHORT).show()).create().show();
            }
        });
    }

    /**
     * Updates the user information in the fragment's View
     * @param user The user pulled from the View Model
     */
    private void updateUserInfo(User user) {
        userId = user.getUserId();

        final EditText nameField = binding.profileEditNameInput;
        final EditText emailField = binding.profileEditEmailInput;
        final EditText phoneField = binding.profileEditPhoneInput;
        final CheckBox optNotifs = binding.profileEditNotifications;
        final CheckBox isOrganizer = binding.profileEditIsOrganizer;
        final ImageView photo = binding.profileEditPhoto;
        final FloatingActionButton removePhoto = binding.profileEditRemovePhoto;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        optNotifs.setChecked(user.isNotificationOptOut());
        isOrganizer.setChecked(user.isOrganizer());
        if (user.hasPhoto()) {
            userHasPhoto = true;
            removePhoto.setVisibility(View.VISIBLE);
            oldPhotoUri = user.getPhotoUri();
            Glide.with(requireContext())
                    .load(user.getPhotoUri())
                    .into(photo);
        } else {
            removePhoto.setVisibility(View.GONE);
            photo.setImageBitmap(PhotoManager.generateDefaultProfilePicture(nameField.getText().toString(), userId));
        }
    }

    /**
     * Verifies if the user is an admin, and if they are, shows them the delete button
     */
    private void verifyDeleteButton(User user) {
        final FloatingActionButton deleteButton = binding.profileEditDelete;
        deleteButton.setVisibility(user.isAdmin()
                && !Objects.equals(user.getUserId(), Objects.requireNonNull(profileViewModel.getUser().getValue()).getUserId())
                ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates the list of facilities to match the facilities owned by the user
     * @param facilities The list of facilities returned by the View Model
     */
    private void updateFacilities(List<Facility> facilities) {
        this.facilities.clear();
        this.facilities.addAll(facilities);
        // TODO: calculating diff with DiffUtil
    }

    /**
     * Gets whether or not the current user has any facilities they manage
     * @return Whether or not the current user has any facilities they manage
     */
    private boolean hasFacilities() {
        return !facilities.isEmpty();
    }

    /**
     * Checks if the edit form is valid to be possible to confirm
     */
    private Confirmed confirmable() {
        final EditText nameField = binding.profileEditNameInput;
        final String name = Objects.requireNonNull(nameField.getText()).toString();
        final EditText emailField = binding.profileEditEmailInput;
        final String email = Objects.requireNonNull(emailField.getText()).toString();
        final EditText phoneField = binding.profileEditPhoneInput;
        final String phone = Objects.requireNonNull(phoneField.getText()).toString();
        final CheckBox isOrganizerCheck = binding.profileEditIsOrganizer;
        final boolean isOrganizer = isOrganizerCheck.isChecked();

        if (name.isEmpty())
            return Confirmed.NAME;
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Confirmed.EMAIL;
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches())
            return Confirmed.PHONE;
        if (!isOrganizer && hasFacilities())
            return Confirmed.ORGANIZER;

        return Confirmed.YES;
    }

    /**
     * Updates the user in the view model, feeding in all changed values in the view
     */
    private void updateUser() {
        // do confirm routine
        try {
            final EditText nameField = binding.profileEditNameInput;
            final EditText emailField = binding.profileEditEmailInput;
            final EditText phoneField = binding.profileEditPhoneInput;
            final CheckBox optNotifs = binding.profileEditNotifications;
            final CheckBox isOrganizer = binding.profileEditIsOrganizer;

            profileViewModel.updateUser(Objects.requireNonNull(nameField.getText()).toString(),
                    Objects.requireNonNull(emailField.getText()).toString(),
                    Objects.requireNonNull(phoneField.getText()).toString(),
                    optNotifs.isChecked(),
                    isOrganizer.isChecked(),
                    photoUriString);
        }  catch (Exception e) {
            Log.e(TAG, "Failure to update user: ", e);
            Toast.makeText(getContext(), R.string.user_update_failed, Toast.LENGTH_SHORT).show();
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
