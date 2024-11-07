package com.example.eventapp;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventapp.models.Signup;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SignupRepositoryTest {

    private SignupRepository signupRepository;

    @Before
    public void setup() {
        signupRepository = FirestoreEmulator.getSignupRepository();
    }

    @After
    public void tearDown() {
        signupRepository = null;
    }

    @Test
    public void testAddSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");

        Task<DocumentReference> addSignupTask = signupRepository.addSignup(signup);
        Tasks.await(addSignupTask);
        DocumentReference docRef = addSignupTask.getResult();

        assertTrue(addSignupTask.isSuccessful());
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test
    public void testUpdateSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");

        Task<DocumentReference> addSignupTask = signupRepository.addSignup(signup);
        Tasks.await(addSignupTask);
        assertTrue(addSignupTask.isSuccessful());

        DocumentReference docRef = addSignupTask.getResult();
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        signup.setDocumentId(docRef.getId());
        signup.setEventId("newEventId");

        Task<Void> updateSignupTask = signupRepository.updateSignup(signup);
        Tasks.await(updateSignupTask);
        assertTrue(updateSignupTask.isSuccessful());

        CompletableFuture<Signup> getSignupFuture = signupRepository.getSignup("testUserId", "newEventId");
        Signup updatedSignup = getSignupFuture.get();
        assertNotNull(updatedSignup);
        assertEquals("newEventId", updatedSignup.getEventId());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test
    public void testRemoveSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        Task<DocumentReference> addSignupTask = signupRepository.addSignup(signup);
        Tasks.await(addSignupTask);
        assertTrue(addSignupTask.isSuccessful());

        DocumentReference docRef = addSignupTask.getResult();
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        signup.setDocumentId(docRef.getId());

        CompletableFuture<Void> removeSignupFuture = signupRepository.removeSignup(signup);
        removeSignupFuture.get();

        CompletableFuture<Signup> getRemovedSignupFuture = signupRepository
                .getSignup("testUserId", "testEventId");
        Signup removedSignup = getRemovedSignupFuture.get();
        assertNull(removedSignup);
    }

    @Test
    public void testGetSignup_existingSignup() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        Task<DocumentReference> addSignupTask = signupRepository.addSignup(signup);
        Tasks.await(addSignupTask);
        assertTrue(addSignupTask.isSuccessful());

        DocumentReference docRef = addSignupTask.getResult();
        assertNotNull(docRef);
        assertNotNull(docRef.getId());

        signup.setDocumentId(docRef.getId());

        CompletableFuture<Signup> getSignupFuture = signupRepository.getSignup("testUserId", "testEventId");
        Signup retrievedSignup = getSignupFuture.get();
        assertNotNull(retrievedSignup);
        assertEquals("testUserId", retrievedSignup.getUserId());
        assertEquals("testEventId", retrievedSignup.getEventId());

        // Cleanup
        Tasks.await(docRef.delete());
    }

    @Test
    public void testGetSignup_nonExistingSignup() throws ExecutionException, InterruptedException {
        CompletableFuture<Signup> getNonExistingSignupFuture = signupRepository
                .getSignup("nonExistingUserId", "nonExistingEventId");

        Signup retrievedSignup = getNonExistingSignupFuture.get();
        assertNull(retrievedSignup);
    }
}
