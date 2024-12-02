package com.example.eventapp.utils;

import android.support.v4.app.INotificationSideChannel;

import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.NotificationRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.repositories.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * FirestoreEmulator provides singleton access to Firestore instances configured for use with a local emulator,
 * along with test instances of various repositories. This utility class ensures that Firestore settings are
 * set to interact with the emulator rather than live Firestore, enabling efficient testing and development.
 * This class includes methods to retrieve singleton instances of repositories for testing purposes, each
 * configured to use the emulator instance.
 */
public class FirestoreEmulator {

    private static FirebaseFirestore emulatorInstance;

    private FirestoreEmulator() {}

    /**
     * Configures and returns a Firestore instance for use with an emulator.
     *
     * @return The configured Firestore instance.
     */
    private static FirebaseFirestore getEmulator() {
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost("10.0.2.2:8080")
                .setSslEnabled(false)
                .build();

        instance.setFirestoreSettings(settings);
        return instance;
    }

    /**
     * Retrieves a singleton instance of the Firestore emulator.
     *
     * @return The singleton Firestore emulator instance.
     */
    public static synchronized FirebaseFirestore getEmulatorInstance() {
        if (emulatorInstance == null) {
            emulatorInstance = getEmulator();
        }
        return emulatorInstance;
    }

    /**
     * Retrieves a test instance of EventRepository using the Firestore emulator.
     *
     * @return The test instance of EventRepository.
     */
    public static synchronized EventRepository getEventRepository() {
        return EventRepository.getTestInstance(getEmulatorInstance());
    }

    /**
     * Retrieves a test instance of FacilityRepository using the Firestore emulator.
     *
     * @return The test instance of FacilityRepository.
     */
    public static synchronized FacilityRepository getFacilityRepository() {
        return FacilityRepository.getTestInstance(getEmulatorInstance());
    }

    /**
     * Retrieves a test instance of SignupRepository using the Firestore emulator.
     *
     * @return The test instance of SignupRepository.
     */
    public static synchronized SignupRepository getSignupRepository() {
        return SignupRepository.getTestInstance(getEmulatorInstance());
    }

    /**
     * Retrieves a test instance of UserRepository using the Firestore emulator.
     *
     * @return The test instance of UserRepository.
     */
    public static synchronized UserRepository getUserRepository() {
        return UserRepository.getTestInstance(getEmulatorInstance());
    }

    /**
     * Retrieves a test instance of NotificationRepository using the Firestore emulator.
     *
     * @return The test instance of NotificationRepository.
     */
    public static synchronized NotificationRepository getNotificationRepository() {
        return NotificationRepository.getTestInstance(getEmulatorInstance());
    }
}
