package com.example.eventapp.viewmodelTests;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Signup;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.DTOs.UserSignupEntry;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.NotificationRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.EntrantsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import com.google.firebase.functions.FirebaseFunctions;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class EntrantsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EntrantsViewModel entrantsViewModel;
    private SignupRepository signupRepository;
    private NotificationRepository notificationRepository;
    private EventRepository eventRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        signupRepository = FirestoreEmulator.getSignupRepository();
        notificationRepository = FirestoreEmulator.getNotificationRepository();
        eventRepository = FirestoreEmulator.getEventRepository();
        FirebaseFunctions firebaseFunctions = FirebaseFunctions.getInstance();

        entrantsViewModel = new EntrantsViewModel(
                signupRepository,
                notificationRepository,
                eventRepository,
                firebaseFunctions
        );
    }

    @After
    public void tearDown() {
        entrantsViewModel = null;
        signupRepository = null;
        notificationRepository = null;
        eventRepository = null;
    }

    @Test
    public void testRemoveSignupEntry_removesEntry() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");
        event.setNumberOfAttendees(10);
        event.setOrganizerId("testOrganizerId");

        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        entrantsViewModel.setCurrentEventToQuery(event);

        User user = new User();
        user.setUserId("user1");
        user.setName("User One");
        user.setEmail("user1@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user1").set(user));

        Signup signup = new Signup("user1", eventId);
        signupRepository.addSignup(signup).get();

        UserSignupEntry entry = new UserSignupEntry(user, "Waitlisted");

        Task<QuerySnapshot> taskBefore = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "user1")
                .whereEqualTo("eventId", eventId)
                .get();
        Tasks.await(taskBefore);
        assertFalse("Signup should exist before removal", taskBefore.getResult().isEmpty());

        entrantsViewModel.removeSignupEntry(entry).get();

        // Check that the signup no longer exists
        Task<QuerySnapshot> taskAfter = firestoreEmulator.collection("signups")
                .whereEqualTo("userId", "user1")
                .whereEqualTo("eventId", eventId)
                .get();
        Tasks.await(taskAfter);
        assertTrue("Signup should be removed", taskAfter.getResult().isEmpty());
    }

    @Test
    public void testCancelEntrants_removesEntries() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");
        event.setNumberOfAttendees(10);
        event.setOrganizerId("testOrganizerId");

        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        entrantsViewModel.setCurrentEventToQuery(event);

        User user1 = new User();
        user1.setUserId("user1");
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user1").set(user1));

        User user2 = new User();
        user2.setUserId("user2");
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user2").set(user2));

        Signup signup1 = new Signup("user1", eventId);
        Signup signup2 = new Signup("user2", eventId);
        signupRepository.addSignup(signup1).get();
        signupRepository.addSignup(signup2).get();

        UserSignupEntry entry1 = new UserSignupEntry(user1, "Waitlisted");
        UserSignupEntry entry2 = new UserSignupEntry(user2, "Waitlisted");

        List<UserSignupEntry> selectedEntrants = Arrays.asList(entry1, entry2);

        // Ensure signups exist
        Task<QuerySnapshot> taskBefore = firestoreEmulator.collection("signups")
                .whereEqualTo("eventId", eventId)
                .get();
        Tasks.await(taskBefore);
        assertEquals(2, taskBefore.getResult().size());

        entrantsViewModel.cancelEntrants(selectedEntrants);
        Thread.sleep(1000);

        // Check that signups are removed
        Task<QuerySnapshot> taskAfter = firestoreEmulator.collection("signups")
                .whereEqualTo("eventId", eventId)
                .get();
        Tasks.await(taskAfter);
        assertEquals(0, taskAfter.getResult().size());
    }

    @Test
    public void testDeleteQrCodeHash_setsQrCodeHashToNull() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");
        event.setNumberOfAttendees(10);
        event.setOrganizerId("testOrganizerId");
        event.setQrCodeHash("someHash");

        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        Event eventFromDb = eventRepository.getEventById(eventId).get();
        assertNotNull("qrCodeHash should be set", eventFromDb.getQrCodeHash());

        entrantsViewModel.setCurrentEventToQuery(event);
        entrantsViewModel.deleteQrCodeHash().get();

        // Check that qrCodeHash is null
        Event updatedEvent = eventRepository.getEventById(eventId).get();
        assertNull("qrCodeHash should be null", updatedEvent.getQrCodeHash());
    }

    @Test
    public void testReAddQrCodeHash_restoresQrCodeHash() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");
        event.setNumberOfAttendees(10);
        event.setOrganizerId("testOrganizerId");

        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        Event eventFromDb = eventRepository.getEventById(eventId).get();
        assertNull("qrCodeHash should be null", eventFromDb.getQrCodeHash());

        entrantsViewModel.setCurrentEventToQuery(event);
        entrantsViewModel.reAddQrCodeHash().get();

        // Check that qrCodeHash is restored
        Event updatedEvent = eventRepository.getEventById(eventId).get();
        assertEquals(eventId + "--display", updatedEvent.getQrCodeHash());
    }

    @Test
    public void testGetCurrentEventToQuery_returnsCorrectEvent() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("testFacilityId");
        event.setNumberOfAttendees(10);
        event.setOrganizerId("testOrganizerId");

        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        entrantsViewModel.setCurrentEventToQuery(event);

        Event currentEvent = entrantsViewModel.getCurrentEventToQuery();

        // Assert that the current event is the same as the event set
        assertNotNull(currentEvent);
        assertEquals(eventId, currentEvent.getDocumentId());
    }
}
