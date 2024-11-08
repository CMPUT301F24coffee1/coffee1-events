package com.example.eventapp.repositoryTests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventapp.models.Facility;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FacilityRepositoryTest {

    private FacilityRepository facilityRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        facilityRepository = FirestoreEmulator.getFacilityRepository();
    }

    @After
    public void tearDown() {
        firestoreEmulator = null;
        facilityRepository = null;
    }

    @Test
    public void testAddFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        CompletableFuture<String> addFacilityFuture = facilityRepository.addFacility(facility);
        String documentId = addFacilityFuture.get();
        assertNotNull("Document ID should not be null", documentId);

        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertTrue("Document should exist in Firestore", snapshot.exists());

        Facility testFacility = snapshot.toObject(Facility.class);
        assertNotNull(testFacility);
        assertEquals("Test Facility", testFacility.getFacilityName());
        assertEquals("Test Description", testFacility.getFacilityDescription());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = ExecutionException.class)
    public void testAddFacility_nullOrganizerId() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.addFacility(facility).get();
    }

    @Test
    public void testUpdateFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Original Name");
        facility.setFacilityDescription("Original Description");

        // Add facility and get document ID
        CompletableFuture<String> addFacilityFuture = facilityRepository.addFacility(facility);
        String documentId = addFacilityFuture.get();
        assertNotNull("Document ID should not be null", documentId);

        facility.setDocumentId(documentId);
        facility.setFacilityName("Updated Name");
        facility.setFacilityDescription("Updated Description");

        // Update facility
        CompletableFuture<Void> updateFacilityFuture = facilityRepository.updateFacility(facility);
        updateFacilityFuture.get();

        // Verify update
        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        Facility updatedFacility = snapshot.toObject(Facility.class);

        assertNotNull("Updated facility should not be null", updatedFacility);
        assertEquals("Updated Name", updatedFacility.getFacilityName());
        assertEquals("Updated Description", updatedFacility.getFacilityDescription());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = ExecutionException.class)
    public void testUpdateFacility_nullDocumentId() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.updateFacility(facility).get();
    }

    @Test
    public void testRemoveFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Facility to be Removed");
        facility.setFacilityDescription("Description");

        // Add facility and get document ID
        CompletableFuture<String> addFacilityFuture = facilityRepository.addFacility(facility);
        String documentId = addFacilityFuture.get();
        assertNotNull("Document ID should not be null", documentId);

        facility.setDocumentId(documentId);

        // Remove facility
        CompletableFuture<Void> removeFacilityFuture = facilityRepository.removeFacility(facility);
        removeFacilityFuture.get();

        // Verify removal
        DocumentReference docRef = firestoreEmulator.collection("facilities").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertFalse("Document should no longer exist in Firestore", snapshot.exists());
    }

    @Test(expected = ExecutionException.class)
    public void testRemoveFacility_nullDocumentId() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.removeFacility(facility).get();
    }
}