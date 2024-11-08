package com.example.eventapp.models;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getName());
        assertFalse(user.isOrganizer());
        assertFalse(user.isAdmin());
        assertEquals("", user.getPhotoUriString());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPhoneNumber());
        assertFalse(user.isNotificationOptOut());
    }

    @Test
    public void testConstructorWithName() {
        User user = new User("Test User");
        assertEquals("Test User", user.getName());
        assertFalse(user.isOrganizer());
    }

    @Test
    public void testConstructorWithNameAndEmail() {
        User user = new User("Test User", "test@example.com");
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("", user.getPhoneNumber());
        assertFalse(user.isNotificationOptOut());
    }

    @Test
    public void testConstructorWithNameEmailAndPhoneNumber() {
        User user = new User("Test User", "test@example.com", "1234567890");
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertFalse(user.isNotificationOptOut());
    }

    @Test
    public void testConstructorWithNameEmailPhoneNumberAndPhoto() {
        User user = new User("Test User", "test@example.com", "1234567890", "uri://photo");
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals("uri://photo", user.getPhotoUriString());
        assertFalse(user.isNotificationOptOut());
    }

    @Test
    public void testConstructorWithNameAndOrganizer() {
        User user = new User("Test User", true);
        assertEquals("Test User", user.getName());
        assertTrue(user.isOrganizer());
    }

    @Test
    public void testConstructorWithNameOrganizerAndAdmin() {
        User user = new User("Test User", true, true);
        assertEquals("Test User", user.getName());
        assertTrue(user.isOrganizer());
        assertTrue(user.isAdmin());
    }

    @Test
    public void testGetAndSetUserId() {
        user.setUserId("user123");
        assertEquals("user123", user.getUserId());
    }

    @Test
    public void testGetAndSetName() {
        user.setName("Updated User");
        assertEquals("Updated User", user.getName());
    }

    @Test
    public void testIsAndSetOrganizer() {
        user.setOrganizer(true);
        assertTrue(user.isOrganizer());
        user.setOrganizer(false);
        assertFalse(user.isOrganizer());
    }

    @Test
    public void testIsAndSetAdmin() {
        user.setAdmin(true);
        assertTrue(user.isAdmin());
        user.setAdmin(false);
        assertFalse(user.isAdmin());
    }

    @Test
    public void testGetAndSetPhotoUriString() {
        user.setPhotoUriString("uri://photo");
        assertEquals("uri://photo", user.getPhotoUriString());
    }

    @Test
    public void testGetPhotoUri() {
        user.setPhotoUriString("https://example.com/photo");
        Uri expectedUri = Uri.parse("https://example.com/photo");
        assertEquals(expectedUri, user.getPhotoUri());
    }

    @Test
    public void testHasPhotoTrue() {
        user.setPhotoUriString("https://example.com/photo");
        assertTrue(user.hasPhoto());
    }

    @Test
    public void testHasPhotoFalse() {
        user.setPhotoUriString("");
        assertFalse(user.hasPhoto());
    }

    @Test
    public void testGetAndSetEmail() {
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    public void testGetAndSetPhoneNumber() {
        user.setPhoneNumber("1234567890");
        assertEquals("1234567890", user.getPhoneNumber());
    }

    @Test
    public void testIsAndSetNotificationOptOut() {
        user.setNotificationOptOut(true);
        assertTrue(user.isNotificationOptOut());
        user.setNotificationOptOut(false);
        assertFalse(user.isNotificationOptOut());
    }
}
