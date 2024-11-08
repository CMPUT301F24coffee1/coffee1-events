package com.example.eventapp.repositoryTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;
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
public class EventRepositoryTest {

    private EventRepository eventRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        eventRepository = FirestoreEmulator.getEventRepository();
    }

    @After
    public void tearDown() {
        firestoreEmulator = null;
        eventRepository = null;
    }

    @Test
    public void testAddEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Test Event");

        CompletableFuture<String> addEventFuture = eventRepository.addEvent(event);
        String documentId = addEventFuture.get();

        assertNotNull("Document ID should not be null", documentId);

        DocumentReference docRef = firestoreEmulator.collection("events").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertTrue("Document should exist in Firestore", snapshot.exists());

        Event testEvent = snapshot.toObject(Event.class);
        assertNotNull(testEvent);
        assertEquals("Test Event", testEvent.getEventName());

        Tasks.await(docRef.delete());
    }

    @Test(expected = ExecutionException.class)
    public void testAddEvent_nullOrganizerId() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setFacilityId("testFacilityId");
        event.setEventName("Test Event without Organizer");

        eventRepository.addEvent(event).get();
    }

    @Test
    public void testUpdateEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Original Title");

        CompletableFuture<String> addEventFuture = eventRepository.addEvent(event);
        String documentId = addEventFuture.get();
        assertNotNull("Document ID should not be null", documentId);

        event.setDocumentId(documentId);
        event.setEventName("Updated Title");

        CompletableFuture<Void> updateEventFuture = eventRepository.updateEvent(event);
        updateEventFuture.get();

        DocumentReference docRef = firestoreEmulator.collection("events").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        Event updatedEvent = snapshot.toObject(Event.class);

        assertNotNull("Updated event should not be null", updatedEvent);
        assertEquals("Updated Title", updatedEvent.getEventName());

        Tasks.await(docRef.delete());
    }

    @Test(expected = ExecutionException.class)
    public void testUpdateEvent_nullDocumentId() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Event without Document ID");

        eventRepository.updateEvent(event).get();
    }

    @Test
    public void testRemoveEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setOrganizerId("testOrganizerId");
        event.setFacilityId("testFacilityId");
        event.setEventName("Event to be Removed");

        CompletableFuture<String> addEventFuture = eventRepository.addEvent(event);
        String documentId = addEventFuture.get();
        assertNotNull("Document ID should not be null", documentId);

        event.setDocumentId(documentId);

        CompletableFuture<Void> removeEventFuture = eventRepository.removeEvent(event);
        removeEventFuture.get();

        DocumentReference docRef = firestoreEmulator.collection("events").document(documentId);
        DocumentSnapshot snapshot = Tasks.await(docRef.get());
        assertFalse("Document should no longer exist in Firestore", snapshot.exists());
    }
}