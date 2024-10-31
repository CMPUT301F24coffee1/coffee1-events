package com.example.eventapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.example.eventapp.models.User;
import com.example.eventapp.photos.DefaultImageUploader;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


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
        createOrLoadCurrentUser(androidId);
        //

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("settings").document("defaultPoster");

        // Upload default poster to db (MUST COMMENT OUT AFTER RUNNING FOR FIRST TIME)
//        docRef.get().addOnCompleteListener(task -> {
//            DefaultImageUploader uploadHelper = new DefaultImageUploader();
//            uploadHelper.uploadImageAndSaveUri();
//
//        });

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