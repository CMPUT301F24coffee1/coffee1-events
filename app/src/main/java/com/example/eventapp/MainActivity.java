package com.example.eventapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import com.example.eventapp.models.Notification;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.notifications.NotificationService;
import com.example.eventapp.services.notifications.ShowNotifications;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    NavController navController;
    Menu navMenu;

    /**
     * Initializes the main activity and sets up Firebase, bindings, navigation, and permission handling.
     * This method handles the configuration of the navigation controller, dynamically adjusts the visibility
     * of navigation elements based on the user's role, and requests photo access permissions if necessary.
     *
     * @param savedInstanceState The saved instance state from a previous activity instance, if any.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        com.example.eventapp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        ConstraintLayout root = binding.getRoot();
        setContentView(root);

        // Initialize the permission launcher to handle permission results
        // Continue with photo access or loading
        ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean readMediaGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
            Boolean readStorageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);

            if (readMediaGranted != null && readMediaGranted || readStorageGranted != null && readStorageGranted) {
                Toast.makeText(this, "Permission granted to access photos", Toast.LENGTH_SHORT).show();
                // Continue with photo access or loading
            } else {
                Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show();
            }
        });

        // No SignupFragment yet
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                root.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        createOrLoadCurrentUser(androidId);

        // UNCOMMENT THIS LINE TO TEST NOTIFICATIONS
        // testUploadNotification(androidId);

        NotificationService notificationService = NotificationService.getInstance();

        // Fetch and show all current users notifications
        if (androidId != null) {
            notificationService.fetchUnreadNotifications(androidId)
                    .thenAccept(notifications -> ShowNotifications.showInAppNotifications(MainActivity.this, notifications))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Failed to fetch notifications:", throwable);
                        return null;
                    });
        } else {
            Log.e(TAG, "User ID is null. Unable to fetch notifications.");
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_events, R.id.navigation_scanqr, R.id.navigation_admin_profiles)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // List of destinations that shouldn't show the nav view
        List<Integer> noNavView = Arrays.asList(R.id.navigation_profile,
                R.id.navigation_profile_edit,
                R.id.navigation_facilities,
                R.id.navigation_facility_edit,
                R.id.navigation_facility_add);

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

        LiveData<User> currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                navView.getMenu().findItem(R.id.navigation_admin_profiles).setVisible(user.isAdmin());
                navView.getMenu().findItem(R.id.navigation_admin_images).setVisible(user.isAdmin());
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
            ProfileViewModel profileViewModel =
                    new ViewModelProvider(this).get(ProfileViewModel.class);
            profileViewModel.setSelectedUser(Objects.requireNonNull(profileViewModel.getActualUser().getValue()));
            navController.navigate(R.id.navigation_profile);
        } else if (item.getItemId() == R.id.navigation_profile_edit) {
            navController.navigate(R.id.navigation_profile_edit);
        } else if (item.getItemId() == android.R.id.home) {
            // Back button
            navController.popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Retrieves an existing user by userId or creates and loads a new user if the user does not exist.
     * If the user is successfully retrieved, it is set as the current user.
     * In case of retrieval failure, a new user is created and loaded.
     *
     * @param userId The unique identifier of the user to retrieve or create.
     */
    private void createOrLoadCurrentUser(String userId) {
        UserRepository userRepository = UserRepository.getInstance();

        userRepository.getUser(userId)
            .thenAccept(user -> {
                if (user == null) {
                    createAndLoadNewUser(userRepository, userId);
                } else {
                    Log.d(TAG, "User already exists with ID: " + userId);
                    userRepository.setCurrentUser(user);
                }
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Failed to retrieve user", throwable);
                Log.d(TAG, "Creating user after failing to retrieve from db");
                createAndLoadNewUser(userRepository, userId);
                return null;
            });
    }

    /**
     * Creates a new user with a randomized name and assigns it the provided userId.
     * After creating the user, it saves the user to the repository and sets it as the current user.
     *
     * @param userRepository The UserRepository instance for saving and setting the current user.
     * @param userId The unique identifier to assign to the newly created user.
     */
    private void createAndLoadNewUser(UserRepository userRepository, String userId) {
        String randomizedName = getRandomAlienName(this);
        User user = new User(randomizedName);
        user.setUserId(userId);

        userRepository.saveUser(user).thenAccept(discard -> {
            userRepository.setCurrentUser(user);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to create organizer with ID: " + user.getUserId());
            return null;
        });
    }

    /**
     * Generates a random alien name (lol) by selecting a random first and last name from the
     * /res/values/alien_names.xml
     * @param context the MainActivity context (needed to access resource files)
     * @return A randomly generated alien name in the format "FirstName LastName"
     */
    public String getRandomAlienName(MainActivity context) {
        // Get the first and last name arrays from resources
        String[] firstNames = context.getResources().getStringArray(R.array.firstNames);
        String[] lastNames = context.getResources().getStringArray(R.array.lastNames);

        // Select a random first name and last name
        Random random = new Random();
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];

        // Return the full name
        return firstName + " " + lastName;
    }

    // Code to test the upload notificatons (Will be deleted once Lottery is finished)
    private void testUploadNotification(String userId) {
        Notification g_notification = new Notification(
                userId,
                "Test General Title",
                "This is a to test the general notification.",
                "General"
        );

        Notification i_notification = new Notification(
                userId,
                "Test Invite Title",
                "This is a to test the invite notification.",
                "Invite"
        );

        NotificationService.getInstance().uploadNotification(g_notification)
                .thenAccept(s -> Log.d(TAG, "Notification uploaded successfully!"))
                .exceptionally(throwable -> {
                    Log.e(TAG, "Failed to upload notification", throwable);
                    return null;
                });
    }
}