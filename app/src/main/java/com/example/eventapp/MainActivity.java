package com.example.eventapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    NavController navController;
    Menu navMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        com.example.eventapp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        ConstraintLayout root = binding.getRoot();
        setContentView(root);

        // No SignupFragment yet
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                root.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        createOrLoadCurrentUser(androidId);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // List of destinations that shouldn't show the nav view
        List<Integer> noNavView = Arrays.asList(R.id.navigation_profile, R.id.navigation_profile_edit);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_profile_edit) {
                // Replace back button with cancel button
                Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_cancel_cross_24dp);
            }

            if (noNavView.contains(destination.getId())) {
                // If a profile view is visible, the nav view should not be
                navView.setVisibility(View.GONE);
            } else {
                // If not, we should see the nav view
                navView.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * Overrides the onCreateOptionsMenu to allow for a second menu to exist in
     * addition to the bottom menu. This menu will add buttons on the top,
     * in this case, the profile button
     * @param menu The menu itself that is being manipulated
     * @return The return is unchanged
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean return_val = super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_menu, menu);
        navMenu = menu;
        return return_val;
    }

    /**
     * Overrides the onOptionsItemSelected, which only affects the top nav menu
     * @param item The selected item in the top nav menu
     * @return The return is unchanged
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Manual navigation is necessary for the top nav bar
        if (item.getItemId() == R.id.navigation_profile) {
            navController.navigate(R.id.navigation_profile);
        } else if (item.getItemId() == R.id.navigation_profile_edit) {
            navController.navigate(R.id.navigation_profile_edit);
        } else if (item.getItemId() != R.id.navigation_profile_confirm) {
            // Back button
            navController.popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createOrLoadCurrentUser(String userId) {
        UserRepository userRepository = UserRepository.getInstance();

        userRepository.getUser(userId)
            .thenAccept(user -> {
                if (user == null) {
                    createAndLoadDevOrganizer(userRepository, userId);
                } else {
                    Log.d(TAG, "User already exists with ID: " + userId);
                    userRepository.setCurrentUser(user);
                }
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Failed to retrieve user", throwable);
                Log.d(TAG, "Creating user after failing to retrieve from db");
                createAndLoadDevOrganizer(userRepository, userId);
                return null;
            });
    }

    private void createAndLoadDevOrganizer(UserRepository userRepository, String userId) {
        User user = new User("DevOrganizer", true);
        user.setUserId(userId);

        userRepository.saveUser(user)
            .addOnCompleteListener(saveUserTask -> {
                if (saveUserTask.isSuccessful()) {
                    Log.i(TAG, "Successfully created organizer with ID: " + user.getUserId());
                    userRepository.setCurrentUser(user);
                } else {
                    Log.e(TAG, "Failed to create organizer with ID: " + user.getUserId());
                }
            });
    }
}