package com.example.eventapp.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SignupTest {

    private Signup signup;

    @Before
    public void setUp() {
        signup = new Signup();
    }

    @Test
    public void testDefaultConstructor() {
        Signup signup = new Signup();
        assertNotNull(signup);
        assertNull(signup.getUserId());
        assertNull(signup.getEventId());
        assertEquals(0, signup.getSignupTimestamp());
        assertFalse(signup.isCancelled());
    }

    @Test
    public void testConstructorWithUserIdAndEventId() {
        Signup signup = new Signup("user123", "event456");
        assertEquals("user123", signup.getUserId());
        assertEquals("event456", signup.getEventId());
        assertEquals(0, signup.getSignupTimestamp());
        assertFalse(signup.isCancelled());
    }

    @Test
    public void testGetAndSetDocumentId() {
        signup.setDocumentId("doc789");
        assertEquals("doc789", signup.getDocumentId());
    }

    @Test
    public void testGetAndSetUserId() {
        signup.setUserId("user123");
        assertEquals("user123", signup.getUserId());
    }

    @Test
    public void testGetAndSetEventId() {
        signup.setEventId("event456");
        assertEquals("event456", signup.getEventId());
    }

    @Test
    public void testGetAndSetSignupTimestamp() {
        long timestamp = 1672531200000L; // Example timestamp
        signup.setSignupTimestamp(timestamp);
        assertEquals(timestamp, signup.getSignupTimestamp());
    }

    @Test
    public void testIsAndSetCancelled() {
        signup.setCancelled(true);
        assertTrue(signup.isCancelled());

        signup.setCancelled(false);
        assertFalse(signup.isCancelled());
    }
}
