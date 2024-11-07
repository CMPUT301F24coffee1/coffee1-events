package com.example.eventapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.eventapp.repositories.UserRepository;
import com.example.eventapp.models.User;
import com.example.eventapp.utils.FirestoreEmulator;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

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

        Task<Void> saveUserTask = userRepository.saveUser(user);
        Tasks.await(saveUserTask);

        assertTrue(saveUserTask.isSuccessful());

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
        Tasks.await(userRepository.removeUser(user));
    }

    @Test(expected = NullPointerException.class)
    public void testSaveUser_nullUser() {
        userRepository.saveUser(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSaveUser_nullUserId() {
        User user = new User();
        user.setName("Test User");

        userRepository.saveUser(user);
    }

    @Test
    public void testRemoveUser_success() throws ExecutionException, InterruptedException {
        User user = new User();
        user.setUserId("testUserId");
        user.setName("Test User");

        // Save user first
        Task<Void> saveUserTask = userRepository.saveUser(user);
        Tasks.await(saveUserTask);
        assertTrue(saveUserTask.isSuccessful());

        // Remove user
        Task<Void> removeUserTask = userRepository.removeUser(user);
        Tasks.await(removeUserTask);
        assertTrue(removeUserTask.isSuccessful());

        // Verify removal
        CompletableFuture<User> getUserFuture = userRepository.getUser("testUserId");
        User savedUser = getUserFuture.get();
        assertNull(savedUser);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveUser_nullUser() {
        userRepository.removeUser((User) null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveUserById_nullUserId() {
        userRepository.removeUser((String) null);
    }

    @Test
    public void testGetUser_existingUser() throws ExecutionException, InterruptedException {
        String userId = "testUserId";

        // Save user first
        User user = new User();
        user.setUserId(userId);
        user.setName("Test User");
        user.setEmail("testuser@example.com");

        Task<Void> saveUserTask = userRepository.saveUser(user);
        Tasks.await(saveUserTask);
        assertTrue(saveUserTask.isSuccessful());

        // Get user
        CompletableFuture<User> getUserFuture = userRepository.getUser(userId);
        User retrievedUser = getUserFuture.get();

        assertNotNull(retrievedUser);
        assertEquals(user.getName(), retrievedUser.getName());
        assertEquals(user.getEmail(), retrievedUser.getEmail());

        // Cleanup
        Tasks.await(userRepository.removeUser(user));
    }

    @Test
    public void testGetUser_nonExistingUser() throws ExecutionException, InterruptedException {
        String userId = "nonExistingUserId";

        // Ensure user does not exist
        assertNull(userRepository.getUser(userId).get());
    }

    @Test(expected = NullPointerException.class)
    public void testGetUser_nullUserId() throws ExecutionException, InterruptedException {
        userRepository.getUser(null);
    }
}

