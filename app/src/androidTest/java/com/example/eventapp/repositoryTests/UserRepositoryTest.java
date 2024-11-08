package com.example.eventapp.repositoryTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

        // Verify user saved correctly
        CompletableFuture<User> getUserFuture = userRepository.getUser("testUserId");
        User savedUser = getUserFuture.get();

        assertNotNull(savedUser);
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertEquals(user.getPhoneNumber(), savedUser.getPhoneNumber());
        assertEquals(user.isOrganizer(), savedUser.isOrganizer());
        assertEquals(user.isAdmin(), savedUser.isAdmin());

        // Cleanup
        userRepository.removeUser(user).get();
    }

    @Test(expected = ExecutionException.class)
    public void testSaveUser_nullUser() throws ExecutionException, InterruptedException {
        userRepository.saveUser(null).get();
    }

    @Test(expected = ExecutionException.class)
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

        // Verify removal
        CompletableFuture<User> getUserFuture = userRepository.getUser("testUserId");
        User savedUser = getUserFuture.get();
        assertNull(savedUser);
    }

    @Test(expected = ExecutionException.class)
    public void testRemoveUser_nullUser() throws ExecutionException, InterruptedException {
        userRepository.removeUser((User) null).get();
    }

    @Test(expected = ExecutionException.class)
    public void testRemoveUserById_nullUserId() throws ExecutionException, InterruptedException {
        userRepository.removeUser((String) null).get();
    }

    @Test
    public void testGetUser_existingUser() throws ExecutionException, InterruptedException {
        String userId = "testUserId";

        // Save user first
        User user = new User();
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("testuser@example.com");

        userRepository.saveUser(user).get();

        // Get user
        CompletableFuture<User> getUserFuture = userRepository.getUser(userId);
        User retrievedUser = getUserFuture.get();

        assertNotNull(retrievedUser);
        assertEquals(user.getName(), retrievedUser.getName());
        assertEquals(user.getEmail(), retrievedUser.getEmail());

        // Cleanup
        userRepository.removeUser(user).get();
    }

    @Test
    public void testGetUser_nonExistingUser() throws ExecutionException, InterruptedException {
        String userId = "nonExistingUserId";

        // Ensure user does not exist
        assertNull(userRepository.getUser(userId).get());
    }

    @Test(expected = NullPointerException.class)
    public void testGetUser_nullUserId() {
        userRepository.getUser(null);
    }
}

