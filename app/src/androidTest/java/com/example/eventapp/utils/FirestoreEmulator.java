package com.example.eventapp.utils;

import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.repositories.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirestoreEmulator {

    private static FirebaseFirestore emulatorInstance;

    private FirestoreEmulator() {}

    private static FirebaseFirestore getEmulator() {
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost("10.0.2.2:8080")
                .setSslEnabled(false)
                .build();

        instance.setFirestoreSettings(settings);
        return instance;
    }

    public static synchronized FirebaseFirestore getEmulatorInstance() {
        if (emulatorInstance == null) {
            emulatorInstance = getEmulator();
        }
        return emulatorInstance;
    }

    public static synchronized EventRepository getEventRepository() {
        return EventRepository.getTestInstance(getEmulatorInstance());
    }

    public static synchronized FacilityRepository getFacilityRepository() {
        return FacilityRepository.getTestInstance(getEmulatorInstance());
    }

    public static synchronized SignupRepository getSignupRepository() {
        return SignupRepository.getTestInstance(getEmulatorInstance());
    }

    public static synchronized UserRepository getUserRepository() {
        return UserRepository.getTestInstance(getEmulatorInstance());
    }
}
