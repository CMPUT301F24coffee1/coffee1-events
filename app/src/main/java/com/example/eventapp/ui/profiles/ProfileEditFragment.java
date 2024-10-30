package com.example.eventapp.ui.profiles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentProfileEditBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ProfileEditFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileEditBinding binding;
    private boolean isConfirmed = false;

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
                    // not the b
                    // ack button
                    isConfirmed = true;
                    String confirmable = confirmable();
                    if (confirmable.equals("confirmed")) {
                        navController.popBackStack();
                    } else {
                        Toast.makeText(getContext(), confirmable, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        binding = FragmentProfileEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                root.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        profileViewModel =
                new ViewModelProvider(this, new ViewModelProvider.Factory() {
                    // Custom user factory to allow androidId to be passed to the ViewModel
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create (@NonNull Class<T> modelClass) {
                        //noinspection unchecked
                        return (T) new ProfileViewModel(androidId);
                    }
                }).get(ProfileViewModel.class);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), this::updateUserInfo);
        return root;
    }

    /**
     * Updates the user information in the fragment's View
     * @param user The user pulled from the View Model
     */
    private void updateUserInfo(User user) {
        final TextInputEditText nameField = binding.profileEditNameInput;
        final EditText emailField = binding.profileEditEmailInput;
        final EditText phoneField = binding.profileEditPhoneInput;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
    }

    /**
     * Checks if the edit form is valid to be possible to confirm
     */
    private String confirmable() {
        final TextInputEditText nameField = binding.profileEditNameInput;
        final String name = Objects.requireNonNull(nameField.getText()).toString();
        final EditText emailField = binding.profileEditEmailInput;
        final String email = Objects.requireNonNull(emailField.getText()).toString();
        final EditText phoneField = binding.profileEditPhoneInput;
        final String phone = Objects.requireNonNull(phoneField.getText()).toString();

        if (name.isEmpty())
            return "Name cannot be empty";
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Email format incorrect";
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches())
            return "Phone Number format incorrect";

        return "confirmed";
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
            final TextInputEditText nameField = binding.profileEditNameInput;
            final EditText emailField = binding.profileEditEmailInput;
            final EditText phoneField = binding.profileEditPhoneInput;

            profileViewModel.updateUser(Objects.requireNonNull(nameField.getText()).toString(),
                    Objects.requireNonNull(emailField.getText()).toString(),
                    Objects.requireNonNull(phoneField.getText()).toString());
        }
        binding = null;
    }
}
