package com.example.eventapp.models;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)  // Specify SDK and suppress the manifest warning
public class FacilityTest {

    private Facility facility;

    @Before
    public void setUp() {
        facility = new Facility();
    }

    @Test
    public void testDefaultConstructor() {
        Facility facility = new Facility();
        assertNotNull(facility);
        assertEquals("", facility.getPhotoUriString());
        assertNull(facility.getFacilityName());
        assertNull(facility.getFacilityDescription());
    }

    @Test
    public void testConstructorWithName() {
        Facility facility = new Facility("Test Facility");
        assertEquals("Test Facility", facility.getFacilityName());
        assertNull(facility.getFacilityDescription());
    }

    @Test
    public void testConstructorWithNameAndDescription() {
        Facility facility = new Facility("Test Facility", "Test Description");
        assertEquals("Test Facility", facility.getFacilityName());
        assertEquals("Test Description", facility.getFacilityDescription());
    }

    @Test
    public void testGetAndSetDocumentId() {
        facility.setDocumentId("doc123");
        assertEquals("doc123", facility.getDocumentId());
    }

    @Test
    public void testGetAndSetOrganizerId() {
        facility.setOrganizerId("organizer123");
        assertEquals("organizer123", facility.getOrganizerId());
    }

    @Test
    public void testGetAndSetFacilityName() {
        facility.setFacilityName("Updated Facility Name");
        assertEquals("Updated Facility Name", facility.getFacilityName());
    }

    @Test
    public void testGetAndSetFacilityDescription() {
        facility.setFacilityDescription("Updated Description");
        assertEquals("Updated Description", facility.getFacilityDescription());
    }

    @Test
    public void testGetAndSetPhotoUriString() {
        facility.setPhotoUriString("uri://photo");
        assertEquals("uri://photo", facility.getPhotoUriString());
    }

    @Test
    public void testGetPhotoUri() {
        facility.setPhotoUriString("https://example.com/photo");
        Uri expectedUri = Uri.parse("https://example.com/photo");
        assertEquals(expectedUri, facility.getPhotoUri());
    }

    @Test
    public void testHasPhotoTrue() {
        facility.setPhotoUriString("https://example.com/photo");
        assertTrue(facility.hasPhoto());
    }

    @Test
    public void testHasPhotoFalse() {
        facility.setPhotoUriString("");
        assertFalse(facility.hasPhoto());
    }
}
