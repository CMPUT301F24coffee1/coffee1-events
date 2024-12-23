package com.example.eventapp;

import static com.example.eventapp.services.photos.PhotoManager.generateDefaultProfilePicture;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.Notification;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.services.NotificationService;
import com.example.eventapp.ui.notifications.NotificationDialogFragment;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eventapp.databinding.ActivityMainBinding;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    NavController navController;
    Menu navMenu;
    LiveData<User> currentUserLiveData;

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
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean readMediaGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
            Boolean readStorageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);

            if (readMediaGranted != null && readMediaGranted || readStorageGranted != null && readStorageGranted) {
                Toast.makeText(this, "Permission granted to access photos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show();
            }
        });

        // Request push notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                root.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        createOrLoadCurrentUser(androidId);

        // UNCOMMENT THIS LINE TO TEST NOTIFICATIONS
        // testUploadNotification(androidId);

        // Fetch and show all current user's notifications
        if (androidId != null) {
            observeNotifications(androidId);
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
                R.id.navigation_facility_add,
                R.id.navigation_create_event,
                R.id.navigation_create_event_dates,
                R.id.navigation_create_event_confirm,
                R.id.navigation_view_entrants);

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

        currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();

        currentUserLiveData.observeForever(user -> {
            if (user != null) {
                displayProfilePicture(user, navMenu);
                navView.getMenu().findItem(R.id.navigation_admin_profiles).setVisible(user.isAdmin());
                navView.getMenu().findItem(R.id.navigation_admin_images).setVisible(user.isAdmin());
                getFcmToken();

                if (androidId != null && !user.isNotificationOptOut()) {
                    observeNotifications(androidId);
                } else {
                    Log.e(TAG, "User ID is null. Unable to fetch notifications.");
                }
            }
        });
    }

    /**
     * Retrieves the Firebase Cloud Messaging (FCM) registration token for the device.
     * Logs the token and sends it to the app's server.
     */
    private void getFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and send token to your app server
                    Log.d(TAG, "FCM Registration Token: " + token);
                    sendRegistrationTokenToServer(token);
                });
    }

    /**
     * Sends the FCM registration token to the app server for future notifications.
     *
     * @param token The FCM token to be sent to the server.
     */
    private void sendRegistrationTokenToServer(String token) {
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.updateUserFcmToken(token)
                .thenAccept(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                .exceptionally(e -> {
                    Log.e(TAG, "Failed to save FCM token", e);
                    return null;
                });
    }

    /**
     * Handles the result of permission requests, such as notification permissions.
     *
     * @param requestCode The request code passed in the permission request.
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the requested permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.d(TAG, "Notification permission denied");
            }
        }
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
        currentUserLiveData = UserRepository.getInstance().getCurrentUserLiveData();
        displayProfilePicture(currentUserLiveData.getValue(), menu);
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

    /**
     * Observes unread notifications for the user and processes them in real-time.
     *
     * @param userId The unique user ID.
     */
    private void observeNotifications(String userId) {
        LiveData<List<Notification>> unreadNotificationsLiveData = NotificationService.getInstance()
                .fetchNotificationsLiveData(userId);

        unreadNotificationsLiveData.observe(this, notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                processNotifications(notifications);
            }
        });
    }

    /**
     * Processes a list of notifications, displaying each notification in an appropriate dialog.
     * Handles both "Invite" and "General" types of notifications by delegating to specific processing methods.
     *
     * @param notifications A list of notifications to process. Can be empty or null.
     */
    private void processNotifications(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            Log.i(TAG, "No notifications found.");
            CompletableFuture.completedFuture(null);
            return;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Notification notification : notifications) {
            futures.add(processNotification(notification));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Processes a single notification and displays the appropriate dialog.
     * For "Invite" notifications, event data is fetched before displaying the dialog.
     *
     * @param notification The notification to process.
     * @return A CompletableFuture that completes when the notification has been processed.
     */
    private CompletableFuture<Void> processNotification(Notification notification) {
        EventRepository eventRepository = EventRepository.getInstance();

        if ("Invite".equals(notification.getType())) {
            return eventRepository.getEventById(notification.getEventId())
                    .thenCompose(event -> {
                        if (event != null) {
                            return showNotificationDialog(notification, event);
                        } else {
                            Log.e(TAG, "Event not found for notification: " + notification.getEventId());
                            return CompletableFuture.completedFuture(null);
                        }
                    })
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Failed to fetch event data for notification: " + notification.getEventId(), throwable);
                        runOnUiThread(() -> Toast.makeText(this, "Error loading event data", Toast.LENGTH_LONG).show());
                        return null;
                    });
        } else {
            return showNotificationDialog(notification, null);
        }
    }

    /**
     * Displays a dialog for the given notification, optionally with associated event data.
     *
     * @param notification The notification to display in the dialog.
     * @param event The event associated with the notification, if applicable. Can be null.
     * @return A CompletableFuture that completes when the dialog has been shown.
     */
    private CompletableFuture<Void> showNotificationDialog(Notification notification, @Nullable Event event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        runOnUiThread(() -> {
            NotificationDialogFragment dialog = NotificationDialogFragment.newInstance(notification, event);
            dialog.show(getSupportFragmentManager(), "NotificationDialog");
            future.complete(null);
        });
        return future;
    }

    /**
     * Displays the Profile Picture in the menu button
     *
     * @param user User to pull the profile picture from
     * @param menu Menu to populate with a profile picture
     */
    private void displayProfilePicture(User user, Menu menu) {
        if (user != null && menu != null) {
            if (menu.findItem(R.id.navigation_profile) != null) {
                if ((user.getPhotoUri() != null) && (!user.getPhotoUriString().isEmpty())) {
                    Glide.with(this).asDrawable().load(user.getPhotoUri()).circleCrop().into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            menu.findItem(R.id.navigation_profile).setIcon(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                } else {
                    Drawable image = new BitmapDrawable(getResources(), generateDefaultProfilePicture(user.getName(), user.getUserId()));
                    menu.findItem(R.id.navigation_profile).setIcon(image);
                }
            }

        }

    }
}