package com.example.eventapp.viewmodelTests;

import static org.junit.Assert.*;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.Notification;
import com.example.eventapp.models.Signup;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.repositories.NotificationRepository;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.services.NotificationService;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.NotificationDialogViewModel;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NotificationDialogViewModelTest {

    @Rule
    public androidx.arch.core.executor.testing.InstantTaskExecutorRule instantTaskExecutorRule = new androidx.arch.core.executor.testing.InstantTaskExecutorRule();

    private NotificationDialogViewModel notificationDialogViewModel;
    private NotificationRepository notificationRepository;
    private SignupRepository signupRepository;
    private EventRepository eventRepository;
    private NotificationService notificationService;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        notificationRepository = FirestoreEmulator.getNotificationRepository();
        signupRepository = FirestoreEmulator.getSignupRepository();
        eventRepository = FirestoreEmulator.getEventRepository();
        notificationService = NotificationService.getInstance();

        notificationDialogViewModel = new NotificationDialogViewModel(
                notificationService,
                signupRepository
        );
    }

    @After
    public void tearDown() {
        notificationDialogViewModel = null;
        notificationRepository = null;
        signupRepository = null;
        eventRepository = null;
        notificationService = null;
    }

    @Test
    public void testUpdateSignupStatus_userAcceptedInvitation_updatesSignup() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("user1");
        user.setName("User One");
        user.setEmail("user1@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user1").set(user));

        Event event = new Event();
        event.setEventName("Test Event");
        event.setFacilityId("facility1");
        event.setNumberOfAttendees(100);
        event.setOrganizerId("organizer1");
        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        Signup signup = new Signup("user1", eventId);
        signupRepository.addSignup(signup).get();

        Notification notification = new Notification("user1", "Invitation", "You are invited to Test Event", eventId);
        String notificationId = notificationRepository.uploadNotification(notification).get();
        notification.setDocumentId(notificationId);

        CompletableFuture<Void> future = notificationDialogViewModel.updateSignupStatus(notification, true);
        future.get();

        Signup updatedSignup = signupRepository.getSignup("user1", eventId).get();
        assertNotNull(updatedSignup);
        assertTrue(updatedSignup.isEnrolled());
        assertFalse(updatedSignup.isCancelled());
    }

    @Test
    public void testUpdateSignupStatus_userDeclinesInvitation_updatesSignup() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("user2");
        user.setName("User Two");
        user.setEmail("user2@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user2").set(user));

        Event event = new Event();
        event.setEventName("Another Event");
        event.setFacilityId("facility2");
        event.setNumberOfAttendees(50);
        event.setOrganizerId("organizer2");
        String eventId = eventRepository.addEvent(event).get();
        event.setDocumentId(eventId);

        Signup signup = new Signup("user2", eventId);
        signupRepository.addSignup(signup).get();

        Notification notification = new Notification("user2", "Invitation", "You are invited to Another Event", eventId);
        String notificationId = notificationRepository.uploadNotification(notification).get();
        notification.setDocumentId(notificationId);

        CompletableFuture<Void> future = notificationDialogViewModel.updateSignupStatus(notification, false);
        future.get();

        Signup updatedSignup = signupRepository.getSignup("user2", eventId).get();
        assertNotNull(updatedSignup);
        assertFalse(updatedSignup.isEnrolled());
        assertTrue(updatedSignup.isCancelled());
    }

    @Test
    public void testUpdateSignupStatus_missingUserIdOrEventId_throwsException() throws ExecutionException, InterruptedException {
        Notification notificationMissingUser = new Notification(null, "Invitation", "Missing userId", null);
        CompletableFuture<Void> futureMissingUser = notificationDialogViewModel.updateSignupStatus(notificationMissingUser, true);
        try {
            futureMissingUser.get();
            fail("Expected ExecutionException due to missing userId and eventId");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("Notification must contain valid userId and eventId.", e.getCause().getMessage());
        }

        Notification notificationMissingEvent = new Notification("user3", "Invitation", "Missing eventId", null);
        CompletableFuture<Void> futureMissingEvent = notificationDialogViewModel.updateSignupStatus(notificationMissingEvent, true);
        try {
            futureMissingEvent.get();
            fail("Expected ExecutionException due to missing eventId");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("Notification must contain valid userId and eventId.", e.getCause().getMessage());
        }
    }

    @Test
    public void testDeleteNotification_nonExistingNotification_handlesGracefully() throws ExecutionException, InterruptedException {
        Notification nonExistingNotification = new Notification("user6", "General", "Non-existing notification");
        nonExistingNotification.setDocumentId("nonExistingId");

        CompletableFuture<Void> future = notificationDialogViewModel.deleteNotification(nonExistingNotification);
        future.get();

        // Ensure notification does not exist
        DocumentSnapshot snapshot = Tasks.await(firestoreEmulator.collection("notifications").document("nonExistingId").get());
        assertFalse(snapshot.exists());
    }
}
