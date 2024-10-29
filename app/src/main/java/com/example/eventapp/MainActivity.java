package com.example.eventapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.example.eventapp.models.Organizer;
import com.example.eventapp.repositories.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;



public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

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
        createUserIfNotExists(androidId);
        //

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    /**
     * Overrides the onCreateOptionsMenu to allow for a second menu to exist in
     * addition to the bottom menu. This menu will add buttons on the top,
     * in this case, the profile button
     * @param menu The menu itself that is being manipulated
     * @return What the super function would have returned regardless (generally, true)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean return_val = super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_menu, menu);
        return return_val;
    }

    private void createUserIfNotExists(String userId) {
        UserRepository userRepository = new UserRepository();

        userRepository.getUser(userId)
            .thenAccept(user -> {
                if (user == null) {
                    createDevOrganizer(userRepository, userId);
                } else {
                    Log.d(TAG, "User already exists with ID: " + userId);
                }
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Failed to retrieve user", throwable);
                Log.d(TAG, "Creating user after failing to retrieve from db");
                createDevOrganizer(userRepository, userId);
                return null;
            });
    }

    private void createDevOrganizer(UserRepository userRepository, String userId) {
        Organizer org = new Organizer("DevOrganizer");
        org.setUserId(userId);

        userRepository.saveUser(org)
            .addOnCompleteListener(saveUserTask -> {
                if (saveUserTask.isSuccessful()) {
                    Log.i(TAG, "Successfully created organizer with ID: " + org.getUserId());
                } else {
                    Log.e(TAG, "Failed to create organizer with ID: " + org.getUserId());
                }
            });
    }
}