package com.example.eventapp.models;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE) // Suppress manifest warning
public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        // Setting up an event instance with minimal details
        event = new Event("Sample Event", "This is a sample event description");
    }

    @Test
    public void testDefaultConstructor() {
        Event event = new Event();
        assertNotNull(event);
    }

    @Test
    public void testConstructorWithNameAndDescription() {
        Event event = new Event("Test Event", "Test Description");
        assertEquals("Test Event", event.getEventName());
        assertEquals("Test Description", event.getEventDescription());
        assertEquals(-1, event.getMaxEntrants());
    }

    @Test
    public void testConstructorWithAllAttributes() {
        long startDate = 1672531200000L; // Arbitrary timestamp
        long endDate = 1672617600000L;   // Arbitrary timestamp
        long deadline = 1672520000000L;  // Arbitrary timestamp

        Event event = new Event("Full Event", "uri://poster", "A full description",
                1,true, 100, startDate, endDate, deadline);

        assertEquals("Full Event", event.getEventName());
        assertEquals("uri://poster", event.getPosterUriString());
        assertEquals("A full description", event.getEventDescription());
        assertEquals(1, event.getNumberOfAttendees());
        assertTrue(event.isGeolocationRequired());
        assertEquals(100, event.getMaxEntrants());
        assertEquals(startDate, event.getStartDate());
        assertEquals(endDate, event.getEndDate());
        assertEquals(deadline, event.getDeadline());
    }

    @Test
    public void testGetAndSetDocumentId() {
        event.setDocumentId("doc123");
        assertEquals("doc123", event.getDocumentId());
    }

    @Test
    public void testGetAndSetOrganizerId() {
        event.setOrganizerId("organizer123");
        assertEquals("organizer123", event.getOrganizerId());
    }

    @Test
    public void testGetAndSetFacilityId() {
        event.setFacilityId("facility123");
        assertEquals("facility123", event.getFacilityId());
    }

    @Test
    public void testGetAndSetEventName() {
        event.setEventName("Updated Event Name");
        assertEquals("Updated Event Name", event.getEventName());
    }

    @Test
    public void testGetAndSetEventDescription() {
        event.setEventDescription("Updated description");
        assertEquals("Updated description", event.getEventDescription());
    }

    @Test
    public void testGetAndSetPosterUriString() {
        event.setPosterUriString("uri://updatedPoster");
        assertEquals("uri://updatedPoster", event.getPosterUriString());
    }

    @Test
    public void testGetPosterUri() {
        // Case 1: Empty posterUriString, should return null
        event.setPosterUriString("");
        assertNull("Expected getPosterUri to return null when posterUriString is empty", event.getPosterUri());

        // Case 2: Valid posterUriString, should return non-null Uri
        event.setPosterUriString("https://example.com/poster");
        assertNotNull("Expected getPosterUri to return non-null when posterUriString is valid", event.getPosterUri());
    }

    @Test
    public void testHasPosterTrue() {
        event.setPosterUriString("uri://poster");
        assertTrue(event.hasPoster());
    }

    @Test
    public void testHasPosterFalse() {
        event.setPosterUriString("");
        assertFalse(event.hasPoster());
    }

    @Test
    public void testIsAndSetGeolocationRequired() {
        event.setGeolocationRequired(true);
        assertTrue(event.isGeolocationRequired());
        event.setGeolocationRequired(false);
        assertFalse(event.isGeolocationRequired());
    }

    @Test
    public void testGetAndSetMaxEntrants() {
        event.setMaxEntrants(150);
        assertEquals(150, event.getMaxEntrants());
    }

    @Test
    public void testGetAndSetStartDate() {
        long startDate = 1672531200000L;
        event.setStartDate(startDate);
        assertEquals(startDate, event.getStartDate());
    }

    @Test
    public void testGetAndSetEndDate() {
        long endDate = 1672617600000L;
        event.setEndDate(endDate);
        assertEquals(endDate, event.getEndDate());
    }

    @Test
    public void testGetAndSetDeadline() {
        long deadline = 1672520000000L;
        event.setDeadline(deadline);
        assertEquals(deadline, event.getDeadline());
    }
}
