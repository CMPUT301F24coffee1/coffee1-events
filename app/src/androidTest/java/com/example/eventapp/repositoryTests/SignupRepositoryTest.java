package com.example.eventapp.repositoryTests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.eventapp.models.Signup;
import com.example.eventapp.repositories.SignupRepository;
import com.example.eventapp.utils.FirestoreEmulator;

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

        String documentId = signupRepository.addSignup(signup).get();
        assertNotNull(documentId);

        assertNotNull(signupRepository.getSignup("testUserId", "testEventId").get());

        // Cleanup
        signupRepository.removeSignup(signup).get();
    }

    @Test
    public void testUpdateSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");

        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);
        signup.setEventId("newEventId");

        signupRepository.updateSignup(signup).get();

        CompletableFuture<Signup> getSignupFuture = signupRepository.getSignup("testUserId", "newEventId");
        Signup updatedSignup = getSignupFuture.get();
        assertNotNull(updatedSignup);
        assertEquals("newEventId", updatedSignup.getEventId());

        // Cleanup
        signupRepository.removeSignup(updatedSignup).get();
    }

    @Test
    public void testRemoveSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);

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
        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);

        CompletableFuture<Signup> getSignupFuture = signupRepository.getSignup("testUserId", "testEventId");
        Signup retrievedSignup = getSignupFuture.get();
        assertNotNull(retrievedSignup);
        assertEquals("testUserId", retrievedSignup.getUserId());
        assertEquals("testEventId", retrievedSignup.getEventId());

        // Cleanup
        signupRepository.removeSignup(retrievedSignup).get();
    }

    @Test
    public void testGetSignup_nonExistingSignup() throws ExecutionException, InterruptedException {
        CompletableFuture<Signup> getNonExistingSignupFuture = signupRepository
                .getSignup("nonExistingUserId", "nonExistingEventId");

        Signup retrievedSignup = getNonExistingSignupFuture.get();
        assertNull(retrievedSignup);
    }
}
