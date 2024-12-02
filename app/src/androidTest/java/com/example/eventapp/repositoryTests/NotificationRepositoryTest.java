package com.example.eventapp.repositoryTests;

import static org.junit.Assert.*;

import com.example.eventapp.models.Notification;
import com.example.eventapp.models.User;
import com.example.eventapp.repositories.NotificationRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.google.android.gms.tasks.Tasks;
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
public class NotificationRepositoryTest {

    private NotificationRepository notificationRepository;
    private FirebaseFirestore firestoreEmulator;

    @Before
    public void setup() {
        firestoreEmulator = FirestoreEmulator.getEmulatorInstance();
        notificationRepository = FirestoreEmulator.getNotificationRepository();
    }

    @After
    public void tearDown() {
        firestoreEmulator = null;
        notificationRepository = null;
    }

    @Test
    public void testUploadNotification_success() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("user1");
        user.setName("User One");
        user.setEmail("user1@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user1").set(user));

        Notification notification = new Notification("user1", "Test Title", "Test Message", "event1");

        CompletableFuture<String> uploadFuture = notificationRepository.uploadNotification(notification);
        String notificationId = uploadFuture.get();

        assertNotNull("Notification ID should not be null", notificationId);

        // Verify the notification exists in Firestore
        DocumentSnapshot snapshot = Tasks.await(
                firestoreEmulator.collection("users")
                        .document("user1")
                        .collection("notifications")
                        .document(notificationId)
                        .get()
        );

        assertTrue("Notification should exist in Firestore", snapshot.exists());
        Notification fetchedNotification = snapshot.toObject(Notification.class);
        assertNotNull(fetchedNotification);
        assertEquals("Test Title", fetchedNotification.getTitle());
        assertEquals("Test Message", fetchedNotification.getMessage());
        assertEquals("event1", fetchedNotification.getEventId());
    }

    @Test(expected = NullPointerException.class)
    public void testUploadNotification_nullNotification_throwsException() throws ExecutionException, InterruptedException {
        notificationRepository.uploadNotification(null).get();
    }

    @Test
    public void testDeleteNotification_success() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("user2");
        user.setName("User Two");
        user.setEmail("user2@example.com");
        Tasks.await(firestoreEmulator.collection("users").document("user2").set(user));

        Notification notification = new Notification("user2", "Delete Title", "Delete Message", "event2");
        String notificationId = notificationRepository.uploadNotification(notification).get();

        DocumentSnapshot snapshotBefore = Tasks.await(
                firestoreEmulator.collection("users")
                        .document("user2")
                        .collection("notifications")
                        .document(notificationId)
                        .get()
        );
        assertTrue("Notification should exist before deletion", snapshotBefore.exists());

        // Delete the notification
        CompletableFuture<Void> deleteFuture = notificationRepository.deleteNotification("user2", notificationId);
        deleteFuture.get();

        DocumentSnapshot snapshotAfter = Tasks.await(
                firestoreEmulator.collection("users")
                        .document("user2")
                        .collection("notifications")
                        .document(notificationId)
                        .get()
        );
        assertFalse("Notification should be deleted from Firestore", snapshotAfter.exists());
    }
}
