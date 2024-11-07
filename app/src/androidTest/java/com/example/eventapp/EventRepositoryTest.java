package com.example.eventapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.SignupRepository;
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
public class EventRepositoryTest {

    private EventRepository eventRepository;
    private SignupRepository signupRepository;

    @Before
    public void setup() {
        eventRepository = FirestoreEmulator.getEventRepository();
        signupRepository = FirestoreEmulator.getSignupRepository();
    }

    @After
    public void tearDown() {
        eventRepository = null;
        signupRepository = null;
    }

    @Test
    public void testAddEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Test Event");

        Task<DocumentReference> addEventTask = eventRepository.addEvent(event);
        Tasks.await(addEventTask);
        DocumentReference docRef = addEventTask.getResult();

        assertTrue(addEventTask.isSuccessful());
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = NullPointerException.class)
    public void testAddEvent_nullOrganizerId() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setFacilityId("testFacilityId");
        event.setEventName("Test Event without Organizer");

        eventRepository.addEvent(event);
    }

    @Test
    public void testUpdateEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Original Title");

        // Add event
        Task<DocumentReference> addEventTask = eventRepository.addEvent(event);
        Tasks.await(addEventTask);
        assertTrue(addEventTask.isSuccessful());

        DocumentReference docRef = addEventTask.getResult();
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        event.setDocumentId(docRef.getId());
        event.setEventName("Updated Title");

        // Update event
        Task<Void> updateEventTask = eventRepository.updateEvent(event);
        Tasks.await(updateEventTask);
        assertTrue(updateEventTask.isSuccessful());

        // Verify update
        Task<DocumentSnapshot> getEventTask = docRef.get();
        Tasks.await(getEventTask);
        Event updatedEvent = getEventTask.getResult().toObject(Event.class);

        assertNotNull(updatedEvent);
        assertEquals("Updated Title", updatedEvent.getEventName());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateEvent_nullDocumentId() {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Event without Document ID");

        eventRepository.updateEvent(event);
    }

    @Test
    public void testRemoveEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Event to be Removed");

        // Add event
        Task<DocumentReference> addEventTask = eventRepository.addEvent(event);
        Tasks.await(addEventTask);
        assertTrue(addEventTask.isSuccessful());

        DocumentReference docRef = addEventTask.getResult();
        event.setDocumentId(docRef.getId());

        // Remove event
        Task<Void> removeEventTask = eventRepository.removeEvent(event);
        Tasks.await(removeEventTask);
        assertTrue(removeEventTask.isSuccessful());

        // Verify removal
        Task<DocumentSnapshot> getEventTask = docRef.get();
        Tasks.await(getEventTask);
        assertFalse(getEventTask.getResult().exists());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveEvent_nullDocumentId() {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Event without Document ID");

        eventRepository.removeEvent(event);
    }
}
