package com.example.eventapp.viewmodelTests;

import static org.junit.Assert.*;

import android.net.Uri;

import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.ImagesViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class ImagesViewModelTest {

    private ImagesViewModel imagesViewModel;
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private FacilityRepository facilityRepository;
    private FirebaseFirestore firestoreEmulator;
    private FirebaseStorage firebaseStorage;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        eventRepository = FirestoreEmulator.getEventRepository();
        userRepository = FirestoreEmulator.getUserRepository();
        facilityRepository = FirestoreEmulator.getFacilityRepository();
        firebaseStorage = FirebaseStorage.getInstance();

        imagesViewModel = new ImagesViewModel(eventRepository, userRepository, facilityRepository);
    }

    @After
    public void tearDown() {
        imagesViewModel = null;
        eventRepository = null;
        userRepository = null;
        facilityRepository = null;
    }

    @Test
    public void testSetSelectedImage_updatesSelectedImageUri() {
        Uri testUri = Uri.parse("https://testimageuri.com/image.jpg");
        imagesViewModel.setSelectedImage(testUri);

        assertEquals(testUri, imagesViewModel.getSelectedImage());
    }

    @Test
    public void testGetImageRemoved_returnsTrueAfterRemoval() throws ExecutionException, InterruptedException {
        Uri testUri = Uri.parse("https://testimageuri.com/image.jpg");
        imagesViewModel.setSelectedImage(testUri);
        imagesViewModel.removeSelectedImage();

        assertTrue(imagesViewModel.getImageRemoved());
    }

    @Test
    public void testIsObjectSelected_returnsTrueAfterObjectSet() throws ExecutionException, InterruptedException {
        Uri testUri = Uri.parse("https://testimageuri.com/image.jpg");
        imagesViewModel.setSelectedImage(testUri);
        imagesViewModel.setSelectedObject(testUri).get();

        assertTrue(imagesViewModel.isObjectSelected());
    }
}
