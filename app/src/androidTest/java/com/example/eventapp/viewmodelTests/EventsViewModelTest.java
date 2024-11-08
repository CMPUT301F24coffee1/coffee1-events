package com.example.eventapp.viewmodelTests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.models.User;
import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Objects;
import java.util.concurrent.ExecutionException;


@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EventsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EventsViewModel eventsViewModel;
    private EventRepository eventRepository;
    private SignupRepository signupRepository;
    private UserRepository userRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        eventRepository = FirestoreEmulator.getEventRepository();
        signupRepository = FirestoreEmulator.getSignupRepository();
        userRepository = FirestoreEmulator.getUserRepository();
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();

        User testUser = new User();
        testUser.setUserId("testUserId");
        MutableLiveData<User> liveData = new MutableLiveData<>();
        liveData.setValue(testUser);

        eventsViewModel = new EventsViewModel(
                eventRepository,
                signupRepository,
                userRepository,
                liveData
        );
    }

    @After
    public void tearDown() {
        eventRepository = null;
        signupRepository = null;
        userRepository = null;
        eventsViewModel = null;
    }

    @Test
    public void testAddEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        Tasks.await(eventsViewModel.addEvent(event));

        assertNotNull(event.getDocumentId());

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);

        assertTrue(getEventTask.getResult().exists());
        assertEquals("Test Event", getEventTask.getResult().toObject(Event.class).getEventName());
    }

    @Test
    public void testRemoveEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        Tasks.await(eventsViewModel.addEvent(event));

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);
        assertTrue(getEventTask.getResult().exists());

        Tasks.await(eventsViewModel.removeEvent(event));

        Task<DocumentSnapshot> getRemovedEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getRemovedEventTask);

        assertFalse("Event should be removed", getRemovedEventTask.getResult().exists());
    }

    @Test
    public void testUpdateEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        Tasks.await(eventsViewModel.addEvent(event));

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);
        assertEquals("Test Event", Objects.requireNonNull(getEventTask.getResult().toObject(Event.class)).getEventName());

        event.setEventName("Updated Event");
        Tasks.await(eventsViewModel.updateEvent(event));

        Task<DocumentSnapshot> getUpdatedEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getUpdatedEventTask);

        Event updatedEvent = getUpdatedEventTask.getResult().toObject(Event.class);
        assertNotNull(updatedEvent);

        assertEquals("Updated Event", updatedEvent.getEventName());
    }

    @Test
    public void testRegisterToEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        Tasks.await(eventsViewModel.addEvent(event));
        assertNotNull(event.getDocumentId());

        Tasks.await(eventsViewModel.registerToEvent(event));

        Task<QuerySnapshot> getSignupTask = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "testUserId")
                .whereEqualTo("eventId", event.getDocumentId())
                .get();
        Tasks.await(getSignupTask);

        assertFalse("Signup should exist", getSignupTask.getResult().isEmpty());
    }

    @Test
    public void testUnregisterFromEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        Tasks.await(eventsViewModel.addEvent(event));
        assertNotNull(event.getDocumentId());

        Tasks.await(eventsViewModel.registerToEvent(event));

        eventsViewModel.unregisterFromEvent(event).get();

        Task<QuerySnapshot> getSignupTask = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "testUserId")
                .whereEqualTo("eventId", event.getDocumentId())
                .get();
        Tasks.await(getSignupTask);

        assertTrue("Signup should be removed", getSignupTask.isSuccessful());
    }
}