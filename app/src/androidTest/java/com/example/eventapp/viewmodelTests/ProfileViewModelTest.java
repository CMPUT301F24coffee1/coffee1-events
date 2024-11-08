package com.example.eventapp.viewmodelTests;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.Facility;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.ProfileViewModel;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ProfileViewModel profileViewModel;
    private UserRepository userRepository;
    private FacilityRepository facilityRepository;
    private FirebaseFirestore firestoreEmulator;
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        userRepository = FirestoreEmulator.getUserRepository();
        facilityRepository = FirestoreEmulator.getFacilityRepository();

        User testUser = new User();
        testUser.setUserId("testUserId");
        currentUserLiveData.setValue(testUser);

        profileViewModel = new ProfileViewModel(
                userRepository,
                facilityRepository,
                currentUserLiveData
        );
    }

    @After
    public void tearDown() {
        userRepository = null;
        facilityRepository = null;
        profileViewModel = null;
    }

    @Test
    public void testAddFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("Test Facility");

        CompletableFuture<String> addFacilityFuture = profileViewModel.addFacility(facility);
        assertNotNull(addFacilityFuture);
        String documentId = addFacilityFuture.get();
        assertNotNull("Document ID should not be null", documentId);
        assertEquals("Document ID should match facility's document ID", documentId, facility.getDocumentId());

        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertTrue("Facility should exist in Firestore", snapshot.exists());

        Facility addedFacility = snapshot.toObject(Facility.class);
        assertNotNull("Facility object should not be null", addedFacility);
        assertEquals("Test Facility", addedFacility.getFacilityName());
    }

    @Test
    public void testUpdateSelectedFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("Test Facility");

        CompletableFuture<String> addFacilityFuture = profileViewModel.addFacility(facility);
        String documentId = addFacilityFuture.get();
        facility.setDocumentId(documentId);

        profileViewModel.setSelectedFacility(facility);
        facility.setFacilityName("Updated Facility");

        CompletableFuture<Void> updateFacilityFuture = profileViewModel.updateSelectedFacility(facility);
        updateFacilityFuture.get();

        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        Facility updatedFacility = snapshot.toObject(Facility.class);

        assertNotNull("Updated facility should not be null", updatedFacility);
        assertEquals("Updated Facility", updatedFacility.getFacilityName());
    }

    @Test
    public void testRemoveSelectedFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("Test Facility");

        CompletableFuture<String> addFacilityFuture = profileViewModel.addFacility(facility);
        String documentId = addFacilityFuture.get();
        facility.setDocumentId(documentId);

        profileViewModel.setSelectedFacility(facility);
        CompletableFuture<Void> removeFacilityFuture = profileViewModel.removeSelectedFacility();
        removeFacilityFuture.get();

        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertFalse("Facility should no longer exist in Firestore", snapshot.exists());
    }

    @Test
    public void testUpdateUser_success() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> updateUserFuture = profileViewModel.updateUser(
                "Updated Name",
                "test@example.com",
                "1234567890",
                true,
                true,
                ""
        );
        updateUserFuture.get();

        DocumentReference userRef = firestoreEmulator.collection("users").document("testUserId");
        DocumentSnapshot snapshot = Tasks.await(userRef.get());
        User updatedUser = snapshot.toObject(User.class);

        assertNotNull("Updated user should not be null", updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("test@example.com", updatedUser.getEmail());
        assertEquals("1234567890", updatedUser.getPhoneNumber());
    }

    @Test
    public void testRemoveUserPhoto_clearsPhotoUri() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("testUserId");
        user.setPhotoUriString("samplePhotoUri");
        currentUserLiveData.setValue(user);

        profileViewModel.removeUserPhoto();

        DocumentReference userRef = firestoreEmulator.collection("users").document("testUserId");
        DocumentSnapshot snapshot = Tasks.await(userRef.get());
        User updatedUser = snapshot.toObject(User.class);

        assertNotNull("Updated user should not be null", updatedUser);
        assertEquals("Photo URI should be cleared", "", updatedUser.getPhotoUriString());
    }
}

