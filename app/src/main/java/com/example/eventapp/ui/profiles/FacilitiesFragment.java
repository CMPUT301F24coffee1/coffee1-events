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

    /**
     * When the view is created, inflates the menu with the facilities set, and then initializes
     * the binding to the fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root of the fragment binding
     */
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

        FragmentFacilitiesBinding binding = FragmentFacilitiesBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    /**
     * When the view is created, populates the facility list RecyclerView
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // set up RecyclerView for facilities
        RecyclerView facilitiesList = view.findViewById(R.id.facilities_list);
        facilitiesList.setLayoutManager(new LinearLayoutManager(getContext()));
        facilities = new ArrayList<>();
        facilitiesAdapter = new FacilitiesAdapter(facilities, this);
        facilitiesList.setAdapter(facilitiesAdapter);

        profileViewModel.getFacilities().observe(getViewLifecycleOwner(), this::updateFacilitiesList);
    }

    /**
     * Updates the facilities list in the fragment when it is changed in the database
     * @param newFacilities The new list of facilities from the database
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateFacilitiesList(List<Facility> newFacilities) {
        facilities.clear();
        facilities.addAll(newFacilities);
        // TODO: calculating diff with DiffUtil
        facilitiesAdapter.notifyDataSetChanged();
    }

    /**
     * Shows information about the facility on click
     * @param facility The facility to get information on
     */
    @Override
    public void onFacilityClick(Facility facility) {
        showFacilityInfoPopup(facility);
    }

    /**
     * Creates the fragment that shows facility information, and then updates the viewModel to select the fragment
     * @param facility The fragment to be shown and selected
     */
    private void showFacilityInfoPopup(Facility facility) {
        FacilityInfoFragment facilityInfoFragment = new FacilityInfoFragment();
        profileViewModel.setSelectedFacility(facility);
        facilityInfoFragment.show(requireActivity().getSupportFragmentManager(), "fragment_info");
    }
}
