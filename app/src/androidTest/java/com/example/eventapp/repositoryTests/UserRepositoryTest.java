package com.example.eventapp.repositoryTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.models.User;
import com.example.eventapp.utils.FirestoreEmulator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class UserRepositoryTest {

    private UserRepository userRepository;

    @Before
    public void setup() {
        userRepository = FirestoreEmulator.getUserRepository();
    }

    @After
    public void tearDown() {
        userRepository = null;
    }

    @Test
    public void testSaveUser_success() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("testUserId");
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user.setPhoneNumber("1234567890");
        user.setOrganizer(false);
        user.setAdmin(false);

        userRepository.saveUser(user).get();

        CompletableFuture<User> getUserFuture = userRepository.getUser("testUserId");
        User savedUser = getUserFuture.get();

        assertNotNull(savedUser);
        assertEquals("Test User", savedUser.getName());
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertEquals("1234567890", savedUser.getPhoneNumber());
        assertFalse(savedUser.isOrganizer());
        assertFalse(savedUser.isAdmin());

        userRepository.removeUser(user).get();
    }

    @Test(expected = NullPointerException.class)
    public void testSaveUser_nullUser() throws ExecutionException, InterruptedException {
        userRepository.saveUser(null).get();
    }

    @Test(expected = NullPointerException.class)
    public void testSaveUser_nullUserId() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setName("Test User");
        userRepository.saveUser(user).get();
    }

    @Test
    public void testRemoveUser_success() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("testUserId");
        user.setName("Test User");

        userRepository.saveUser(user).get();

        userRepository.removeUser(user).get();

        CompletableFuture<User> getUserFuture = userRepository.getUser("testUserId");
        assertNull(getUserFuture.get());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveUser_nullUser() throws ExecutionException, InterruptedException {
        userRepository.removeUser((User) null).get();
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveUserById_nullUserId() throws ExecutionException, InterruptedException {
        userRepository.removeUser((String) null).get();
    }

    @Test(expected = ExecutionException.class)
    public void testRemoveUser_currentLoggedInUser() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("testId");
        user.setName("Current User");

        userRepository.saveUser(user).get();
        userRepository.setCurrentUser(user);

        userRepository.removeUser(user).get();
    }

    @Test
    public void testGetUser_existingUser() throws ExecutionException, InterruptedException {
        String userId = "testUserId";

        User user = new User();
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("testuser@example.com");

        userRepository.saveUser(user).get();

        CompletableFuture<User> getUserFuture = userRepository.getUser(userId);
        User retrievedUser = getUserFuture.get();

        assertNotNull(retrievedUser);
        assertEquals("Test User", retrievedUser.getName());
        assertEquals("testuser@example.com", retrievedUser.getEmail());

        userRepository.removeUser(user).get();
    }

    @Test
    public void testGetUser_nonExistingUser() throws ExecutionException, InterruptedException {
        String userId = "nonExistingUserId";
        assertNull(userRepository.getUser(userId).get());
    }

    @Test(expected = NullPointerException.class)
    public void testGetUser_nullUserId() {
        userRepository.getUser(null);
    }

    @Test
    public void testSaveAndRetrieveComplexUserObject() throws ExecutionException, InterruptedException {
        User complexUser = new User();
        complexUser.setUserId("complexUserId");
        complexUser.setName("Complex User");
        complexUser.setEmail("complexuser@example.com");
        complexUser.setPhoneNumber("9876543210");
        complexUser.setOrganizer(true);
        complexUser.setAdmin(true);

        userRepository.saveUser(complexUser).get();

        CompletableFuture<User> getUserFuture = userRepository.getUser("complexUserId");
        User retrievedUser = getUserFuture.get();

        assertNotNull(retrievedUser);
        assertEquals("Complex User", retrievedUser.getName());
        assertEquals("complexuser@example.com", retrievedUser.getEmail());
        assertEquals("9876543210", retrievedUser.getPhoneNumber());
        assertTrue(retrievedUser.isOrganizer());
        assertTrue(retrievedUser.isAdmin());

        userRepository.removeUser(complexUser).get();
    }
}

