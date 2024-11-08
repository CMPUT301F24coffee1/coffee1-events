package com.example.eventapp.viewmodelTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EventsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EventsViewModel eventsViewModel;
    private EventRepository eventRepository;
    private SignupRepository signupRepository;
    private UserRepository userRepository;
    private FirebaseFirestore firestoreEmulator;
    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();

    @Before
    public void setup() {
        eventRepository = FirestoreEmulator.getEventRepository();
        signupRepository = FirestoreEmulator.getSignupRepository();
        userRepository = FirestoreEmulator.getUserRepository();
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();

        User testUser = new User();
        testUser.setUserId("testUserId");
        currentUserLiveData.setValue(testUser);

        eventsViewModel = new EventsViewModel(
                eventRepository,
                signupRepository,
                userRepository,
                currentUserLiveData
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

        eventsViewModel.addEvent(event).get();

        assertNotNull(event.getDocumentId());

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);

        assertTrue(getEventTask.getResult().exists());
        Event addedEvent = getEventTask.getResult().toObject(Event.class);

        assertNotNull(addedEvent);
        assertEquals("Test Event", addedEvent.getEventName());
    }

    @Test
    public void testRemoveEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        eventsViewModel.addEvent(event).get();

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);
        assertTrue(getEventTask.getResult().exists());

        eventsViewModel.removeEvent(event).get();

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

        eventsViewModel.addEvent(event).get();

        Task<DocumentSnapshot> getEventTask = firestoreEmulator.collection("events")
                .document(event.getDocumentId())
                .get();
        Tasks.await(getEventTask);
        assertEquals("Test Event", Objects.requireNonNull(getEventTask.getResult().toObject(Event.class)).getEventName());

        event.setEventName("Updated Event");
        eventsViewModel.updateEvent(event).get();

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

        eventsViewModel.addEvent(event).get();
        assertNotNull(event.getDocumentId());

        eventsViewModel.registerToEvent(event).get();

        Task<QuerySnapshot> getSignupTask = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "testUserId")
                .whereEqualTo("eventId", event.getDocumentId())
                .get();
        Tasks.await(getSignupTask);

        assertTrue(getSignupTask.isSuccessful());
        assertFalse("Signup should exist", getSignupTask.getResult().isEmpty());
    }

    @Test
    public void testUnregisterFromEvent_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");

        eventsViewModel.addEvent(event).get();
        assertNotNull(event.getDocumentId());

        eventsViewModel.registerToEvent(event).get();

        eventsViewModel.unregisterFromEvent(event).get();

        Task<QuerySnapshot> getSignupTask = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "testUserId")
                .whereEqualTo("eventId", event.getDocumentId())
                .get();
        Tasks.await(getSignupTask);

        assertTrue(getSignupTask.isSuccessful());
        assertTrue("Signup should be removed", getSignupTask.getResult().isEmpty());
    }

    @Test
    public void testIsUserOrganizerOrAdmin_adminUser() {
        User adminUser = new User();
        adminUser.setAdmin(true);
        adminUser.setOrganizer(false);
        currentUserLiveData.setValue(adminUser);

        assertTrue("User is admin, should return true", eventsViewModel.isUserOrganizerOrAdmin());
    }

    @Test
    public void testIsUserOrganizerOrAdmin_organizerUser() {
        User organizerUser = new User();
        organizerUser.setAdmin(false);
        organizerUser.setOrganizer(true);
        currentUserLiveData.setValue(organizerUser);

        assertTrue("User is organizer, should return true", eventsViewModel.isUserOrganizerOrAdmin());
    }

    @Test
    public void testIsUserOrganizerOrAdmin_nonAdminNonOrganizerUser() {
        User regularUser = new User();
        regularUser.setAdmin(false);
        regularUser.setOrganizer(false);
        currentUserLiveData.setValue(regularUser);

        assertFalse("User is neither admin nor organizer, should return false", eventsViewModel.isUserOrganizerOrAdmin());
    }

    @Test
    public void testIsUserOrganizerOrAdmin_nullUser() {
        currentUserLiveData.setValue(null);

        assertFalse("User is null, should return false", eventsViewModel.isUserOrganizerOrAdmin());
    }
}