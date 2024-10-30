package com.example.eventapp.ui.profiles;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.example.eventapp.databinding.FragmentProfileBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    /**
     * Behaviour to run when the View is created, in this case,
     * creating the View Model, linking it, and using the View Model
     * to fill out user information for the profile
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

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String androidId = Settings.Secure.getString(
                root.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        ProfileViewModel profileViewModel =
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

    private void updateUserInfo(User user) {
        final TextView nameField = binding.profileName;
        final TextView emailField = binding.profileEmail;
        final TextView phoneField = binding.profilePhone;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
