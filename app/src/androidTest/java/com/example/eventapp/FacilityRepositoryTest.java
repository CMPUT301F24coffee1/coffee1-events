package com.example.eventapp;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.eventapp.models.Facility;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FacilityRepositoryTest {

    private FacilityRepository facilityRepository;

    @Before
    public void setup() {
        facilityRepository = FirestoreEmulator.getFacilityRepository();
    }

    @After
    public void tearDown() {
        facilityRepository = null;
    }

    @Test
    public void testAddFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        Task<DocumentReference> addFacilityTask = facilityRepository.addFacility(facility);
        Tasks.await(addFacilityTask);
        DocumentReference docRef = addFacilityTask.getResult();

        assertTrue(addFacilityTask.isSuccessful());
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = NullPointerException.class)
    public void testAddFacility_nullOrganizerId() {
        Facility facility = new Facility();
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.addFacility(facility);
    }

    @Test
    public void testUpdateFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Original Name");
        facility.setFacilityDescription("Original Description");

        // Add facility
        Task<DocumentReference> addFacilityTask = facilityRepository.addFacility(facility);
        Tasks.await(addFacilityTask);
        assertTrue(addFacilityTask.isSuccessful());

        DocumentReference docRef = addFacilityTask.getResult();
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        facility.setDocumentId(docRef.getId());
        facility.setFacilityName("Updated Name");
        facility.setFacilityDescription("Updated Description");

        // Update facility
        Task<Void> updateFacilityTask = facilityRepository.updateFacility(facility);
        Tasks.await(updateFacilityTask);
        assertTrue(updateFacilityTask.isSuccessful());

        // Verify update
        Task<DocumentSnapshot> getFacilityTask = docRef.get();
        Tasks.await(getFacilityTask);
        Facility updatedFacility = getFacilityTask.getResult().toObject(Facility.class);

        assertNotNull(updatedFacility);
        assertEquals("Updated Name", updatedFacility.getFacilityName());
        assertEquals("Updated Description", updatedFacility.getFacilityDescription());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateFacility_nullDocumentId() {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.updateFacility(facility);
    }

    @Test
    public void testRemoveFacility_success() throws ExecutionException, InterruptedException {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Facility to be Removed");
        facility.setFacilityDescription("Description");

        // Add facility
        Task<DocumentReference> addFacilityTask = facilityRepository.addFacility(facility);
        Tasks.await(addFacilityTask);
        assertTrue(addFacilityTask.isSuccessful());

        DocumentReference docRef = addFacilityTask.getResult();
        facility.setDocumentId(docRef.getId());

        // Remove facility
        Task<Void> removeFacilityTask = facilityRepository.removeFacility(facility);
        Tasks.await(removeFacilityTask);
        assertTrue(removeFacilityTask.isSuccessful());

        // Verify removal
        Task<DocumentSnapshot> getFacilityTask = docRef.get();
        Tasks.await(getFacilityTask);
        assertFalse(getFacilityTask.getResult().exists());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveFacility_nullDocumentId() {
        Facility facility = new Facility();
        facility.setOrganizerId("testOrganizerId");
        facility.setFacilityName("Test Facility");
        facility.setFacilityDescription("Test Description");

        facilityRepository.removeFacility(facility);
    }
}
