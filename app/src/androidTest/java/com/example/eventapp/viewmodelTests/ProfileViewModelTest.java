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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ProfileViewModel profileViewModel;
    private UserRepository userRepository;
    private FacilityRepository facilityRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        userRepository = FirestoreEmulator.getUserRepository();
        facilityRepository = FirestoreEmulator.getFacilityRepository();

        User testUser = new User();
        testUser.setUserId("testUserId");
        MutableLiveData<User> liveData = new MutableLiveData<>();
        liveData.setValue(testUser);

        profileViewModel = new ProfileViewModel(
                userRepository,
                facilityRepository,
                liveData
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

        Task<String> addFacilityTask = profileViewModel.addFacility(facility);
        assertNotNull(addFacilityTask);
        String documentId = Tasks.await(addFacilityTask);
        assertNotNull(documentId);
        assertEquals(documentId, facility.getDocumentId());

        Task<DocumentSnapshot> getFacilityTask = firestoreEmulator.collection("facilities")
                .document(documentId)
                .get();
        Tasks.await(getFacilityTask);

        assertTrue(getFacilityTask.getResult().exists());

        Facility addedFacility = getFacilityTask.getResult().toObject(Facility.class);
        assertNotNull(addedFacility);
        assertEquals("Test Facility", addedFacility.getFacilityName());
    }

    @Test
    public void testUpdateSelectedFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("Test Facility");

        Task<String> addFacilityTask = profileViewModel.addFacility(facility);
        String documentId = Tasks.await(addFacilityTask);
        facility.setDocumentId(documentId);

        profileViewModel.setSelectedFacility(facility);

        facility.setFacilityName("Updated Facility");
        Task<Void> updateFacilityTask = profileViewModel.updateSelectedFacility(facility);
        Tasks.await(updateFacilityTask);

        Task<DocumentSnapshot> getFacilityTask = firestoreEmulator.collection("facilities")
                .document(documentId)
                .get();
        Tasks.await(getFacilityTask);

        Facility updatedFacility = getFacilityTask.getResult().toObject(Facility.class);
        assertNotNull(updatedFacility);
        assertEquals("Updated Facility", updatedFacility.getFacilityName());
    }

    @Test
    public void testRemoveSelectedFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility("Test Facility");

        Task<String> addFacilityTask = profileViewModel.addFacility(facility);
        String documentId = Tasks.await(addFacilityTask);
        facility.setDocumentId(documentId);

        profileViewModel.setSelectedFacility(facility);

        Task<Void> removeFacilityTask = profileViewModel.removeSelectedFacility();
        Tasks.await(removeFacilityTask);

        Task<DocumentSnapshot> getFacilityTask = firestoreEmulator.collection("facilities")
                .document(documentId)
                .get();
        Tasks.await(getFacilityTask);
        assertFalse("Facility should be removed", getFacilityTask.getResult().exists());
    }

    @Test
    public void testUpdateUser_success() throws ExecutionException, InterruptedException {
        Task<Void> updateUserTask = profileViewModel.updateUser("Updated Name", "test@example.com", "1234567890", true, true, "");
        Tasks.await(updateUserTask);

        Task<DocumentSnapshot> getUserTask = firestoreEmulator.collection("users")
                .document("testUserId")
                .get();
        Tasks.await(getUserTask);

        User updatedUser = getUserTask.getResult().toObject(User.class);
        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
    }
}

