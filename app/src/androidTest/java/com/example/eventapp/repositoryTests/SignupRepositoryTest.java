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
        assertNotNull("Document ID should not be null", documentId);

        Signup retrievedSignup = signupRepository.getSignup("testUserId", "testEventId").get();
        assertNotNull("Retrieved signup should not be null", retrievedSignup);
        assertEquals("testUserId", retrievedSignup.getUserId());
        assertEquals("testEventId", retrievedSignup.getEventId());

        // Cleanup
        signupRepository.removeSignup(retrievedSignup).get();
    }

    @Test(expected = ExecutionException.class)
    public void testAddSignup_nullUserId() throws ExecutionException, InterruptedException {
        Signup signup = new Signup(null, "testEventId");
        signupRepository.addSignup(signup).get();
    }

    @Test(expected = ExecutionException.class)
    public void testAddSignup_nullEventId() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", null);
        signupRepository.addSignup(signup).get();
    }

    @Test(expected = NullPointerException.class)
    public void testAddSignup_nullSignup() throws ExecutionException, InterruptedException {
        signupRepository.addSignup(null).get();
    }

    @Test
    public void testUpdateSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);
        signup.setEventId("newEventId");

        signupRepository.updateSignup(signup).get();

        Signup updatedSignup = signupRepository.getSignup("testUserId", "newEventId").get();
        assertNotNull("Updated signup should not be null", updatedSignup);
        assertEquals("newEventId", updatedSignup.getEventId());

        // Cleanup
        signupRepository.removeSignup(updatedSignup).get();
    }

    @Test(expected = ExecutionException.class)
    public void testUpdateSignup_nullDocumentId() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        signupRepository.updateSignup(signup).get();
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateSignup_nullSignup() throws ExecutionException, InterruptedException {
        signupRepository.updateSignup(null).get();
    }

    @Test
    public void testRemoveSignup_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);

        signupRepository.removeSignup(signup).get();

        Signup removedSignup = signupRepository.getSignup("testUserId", "testEventId").get();
        assertNull("Signup should have been removed", removedSignup);
    }

    @Test
    public void testRemoveSignupByUserIdAndEventId_success() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        String documentId = signupRepository.addSignup(signup).get();
        assertNotNull("Document ID should not be null", documentId);

        signupRepository.removeSignup("testUserId", "testEventId").get();

        Signup removedSignup = signupRepository.getSignup("testUserId", "testEventId").get();
        assertNull("Signup should have been removed", removedSignup);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveSignup_nullSignup() throws ExecutionException, InterruptedException {
        signupRepository.removeSignup(null).get();
    }

    @Test
    public void testGetSignup_existingSignup() throws ExecutionException, InterruptedException {
        Signup signup = new Signup("testUserId", "testEventId");
        String documentId = signupRepository.addSignup(signup).get();
        signup.setDocumentId(documentId);

        Signup retrievedSignup = signupRepository.getSignup("testUserId", "testEventId").get();
        assertNotNull("Retrieved signup should not be null", retrievedSignup);
        assertEquals("testUserId", retrievedSignup.getUserId());
        assertEquals("testEventId", retrievedSignup.getEventId());

        // Cleanup
        signupRepository.removeSignup(retrievedSignup).get();
    }

    @Test
    public void testGetSignup_nonExistingSignup() throws ExecutionException, InterruptedException {
        Signup retrievedSignup = signupRepository.getSignup("nonExistingUserId", "nonExistingEventId").get();
        assertNull("Signup should be null for non-existing userId and eventId", retrievedSignup);
    }
}
