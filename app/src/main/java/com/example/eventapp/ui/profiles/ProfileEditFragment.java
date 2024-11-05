package com.example.eventapp.ui.profiles;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentProfileEditBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.viewmodels.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileEditFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileEditBinding binding;
    private List<Facility> facilities;
    private boolean isConfirmed = false;

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
                    isConfirmed = false;
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
                        navController.popBackStack();
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

        return root;
    }

    /**
     * Updates the user information in the fragment's View
     * @param user The user pulled from the View Model
     */
    private void updateUserInfo(User user) {
        final EditText nameField = binding.profileEditNameInput;
        final EditText emailField = binding.profileEditEmailInput;
        final EditText phoneField = binding.profileEditPhoneInput;
        final CheckBox optNotifs = binding.profileEditNotifications;
        final CheckBox isOrganizer = binding.profileEditIsOrganizer;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        optNotifs.setChecked(user.isNotificationOptOut());
        isOrganizer.setChecked(user.isOrganizer());
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
     * Makes sure to clear the binding, and, if confirm button was pressed,
     * update the User in the View Model with the new information
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isConfirmed) {
            // do confirm routine
            final EditText nameField = binding.profileEditNameInput;
            final EditText emailField = binding.profileEditEmailInput;
            final EditText phoneField = binding.profileEditPhoneInput;
            final CheckBox optNotifs = binding.profileEditNotifications;
            final CheckBox isOrganizer = binding.profileEditIsOrganizer;

            profileViewModel.updateUser(Objects.requireNonNull(nameField.getText()).toString(),
                    Objects.requireNonNull(emailField.getText()).toString(),
                    Objects.requireNonNull(phoneField.getText()).toString(),
                    optNotifs.isChecked(),
                    isOrganizer.isChecked());
        }
        binding = null;
    }
}
