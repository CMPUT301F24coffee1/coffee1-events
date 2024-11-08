package com.example.eventapp.ui.profiles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentFacilitiesBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.viewmodels.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class FacilitiesFragment extends Fragment implements
        FacilitiesAdapter.OnFacilityClickListener {

    private ProfileViewModel profileViewModel;
    private ArrayList<Facility> facilities;
    private FacilitiesAdapter facilitiesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        profileViewModel =
                new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Inflate the menu with the profile edit button set
        NavController navController = NavHostFragment.findNavController(this);

        // Inflate the menu with the profile button set (in this case, the edit button)
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_nav_menu_facilities, menu);
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_facilities_add) {
                    navController.navigate(R.id.navigation_facility_add);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        com.example.eventapp.databinding.FragmentFacilitiesBinding binding = FragmentFacilitiesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // set up RecyclerView for facilities
        RecyclerView facilitiesList = view.findViewById(R.id.facilities_list);
        facilitiesList.setLayoutManager(new LinearLayoutManager(getContext()));
        facilities = new ArrayList<>();
        facilitiesAdapter = new FacilitiesAdapter(facilities, this);
        facilitiesList.setAdapter(facilitiesAdapter);

        profileViewModel.getFacilities().observe(getViewLifecycleOwner(), this::updateFacilitiesList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFacilitiesList(List<Facility> newFacilities) {
        facilities.clear();
        facilities.addAll(newFacilities);
        // TODO: calculating diff with DiffUtil
        facilitiesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFacilityClick(Facility facility) {
        showFacilityInfoPopup(facility);
    }

    private void showFacilityInfoPopup(Facility facility) {
        FacilityInfoFragment facilityInfoFragment = new FacilityInfoFragment();
        profileViewModel.setSelectedFacility(facility);
        facilityInfoFragment.show(requireActivity().getSupportFragmentManager(), "fragment_info");
    }
}
