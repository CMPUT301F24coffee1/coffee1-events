package com.example.eventapp.ui.profiles;

import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;
import com.example.eventapp.databinding.FragmentFacilityAddBinding;
import com.example.eventapp.models.Facility;
import com.example.eventapp.viewmodels.ProfileViewModel;

import java.util.Objects;

public class FacilityAddFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentFacilityAddBinding binding;
    private Facility facility;
    private boolean isConfirmed = false;

    private enum Confirmed { YES, NAME }

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
                    isConfirmed = false;
                    FacilityAddFragment.Confirmed confirmable = confirmable();
                    final String error; // Array for finality in lambda statement
                    if (Objects.requireNonNull(confirmable) == Confirmed.NAME) {
                        error = getString(R.string.facility_name_cannot_be_empty);
                    } else {
                        error = ""; // No error
                        isConfirmed = true;
                    }
                    if (isConfirmed) {
                        navController.popBackStack();
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

        facility = profileViewModel.getSelectedFacility(); // Since there is no facility, it will
        // make a blank one

        final EditText nameField = binding.facilityAddNameInput;
        final EditText descField = binding.facilityAddDescInput;

        nameField.setText(facility.getFacilityName());
        descField.setText(facility.getFacilityDescription());

        return root;
    }

    /**
     * Checks if the edit form is valid to be possible to confirm
     */
    private FacilityAddFragment.Confirmed confirmable() {
        final EditText nameField = binding.facilityAddNameInput;
        final String name = Objects.requireNonNull(nameField.getText()).toString();

        if (name.isEmpty())
            return FacilityAddFragment.Confirmed.NAME;

        return FacilityAddFragment.Confirmed.YES;
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
            final EditText nameField = binding.facilityAddNameInput;
            final EditText descField = binding.facilityAddDescInput;

            facility.setFacilityName(nameField.getText().toString());
            facility.setFacilityDescription(descField.getText().toString());

            profileViewModel.addFacility(facility);
        }
        binding = null;
    }
}
