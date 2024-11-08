package com.example.eventapp.ui.profiles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentAdminProfilesBinding;
import com.example.eventapp.models.User;
import com.example.eventapp.viewmodels.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays and manages the list of user profiles for administrators.
 * Integrates with a {@link ProfileViewModel} to observe and display users in a RecyclerView.
 * Enables profile interaction through {@link ProfilesAdapter.OnProfileClickListener}, allowing
 * administrators to view detailed information for individual users.
 *
 * This fragment observes live data from the ProfileViewModel, updating the displayed user list
 * dynamically. When a profile is clicked, it opens a {@link ProfileInfoFragment} as a popup
 * to show more details.
 */
public class AdminProfilesFragment extends Fragment implements
        ProfilesAdapter.OnProfileClickListener {

    private ProfileViewModel profileViewModel;
    private ArrayList<User> Profiles;
    private ProfilesAdapter ProfilesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        profileViewModel =
                new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        com.example.eventapp.databinding.FragmentAdminProfilesBinding binding = FragmentAdminProfilesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // set up RecyclerView for Profiles
        RecyclerView ProfilesList = view.findViewById(R.id.admin_profiles_list);
        ProfilesList.setLayoutManager(new LinearLayoutManager(getContext()));
        Profiles = new ArrayList<>();
        ProfilesAdapter = new ProfilesAdapter(Profiles, this);
        ProfilesList.setAdapter(ProfilesAdapter);

        profileViewModel.getUsers().observe(getViewLifecycleOwner(), this::updateProfilesList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateProfilesList(List<User> newProfiles) {
        Profiles.clear();
        Profiles.addAll(newProfiles);
        // TODO: calculating diff with DiffUtil
        ProfilesAdapter.notifyDataSetChanged();
    }

    public void onProfileClick(User user) {
        showProfileInfoPopup(user);
    }

    private void showProfileInfoPopup(User user) {
        ProfileInfoFragment profileInfoFragment = new ProfileInfoFragment();
        profileViewModel.setSelectedUser(user);
        profileInfoFragment.show(requireActivity().getSupportFragmentManager(), "fragment_profile_info");
    }
}
