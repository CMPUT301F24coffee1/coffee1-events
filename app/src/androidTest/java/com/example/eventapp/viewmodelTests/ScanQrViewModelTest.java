package com.example.eventapp.viewmodelTests;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.EventRepository;
import com.example.eventapp.utils.FirestoreEmulator;
import com.example.eventapp.viewmodels.ScanQrViewModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ScanQrViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScanQrViewModel scanQrViewModel;
    private EventRepository eventRepository;

    @Before
    public void setup() {
        eventRepository = FirestoreEmulator.getEventRepository();
        scanQrViewModel = new ScanQrViewModel(eventRepository);
    }

    @After
    public void tearDown() {
        eventRepository = null;
        scanQrViewModel = null;
    }

    @Test
    public void testGetEventByQrCodeHash_success() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setEventName("Test Event");
        event.setOrganizerId("testOrganizerId");

        String documentId = eventRepository.addEvent(event).get();
        assertNotNull("Document ID should not be null", documentId);

        event.setDocumentId(documentId);
        String qrCodeHash = documentId + "--display";
        event.setQrCodeHash(qrCodeHash);
        eventRepository.updateEvent(event).get();

        Event retrievedEvent = scanQrViewModel.getEventByQrCodeHash(qrCodeHash).get();

        assertNotNull("Retrieved event should not be null", retrievedEvent);
        assertEquals("Event name should match", "Test Event", retrievedEvent.getEventName());
        assertEquals("QR code hash should match", qrCodeHash, retrievedEvent.getQrCodeHash());
        assertEquals("Document ID should match", documentId, retrievedEvent.getDocumentId());

        // Cleanup
        eventRepository.removeEvent(event).get();
    }

    @Test
    public void testGetEventByQrCodeHash_noEventFound() throws ExecutionException, InterruptedException {
        String qrCodeHash = "nonexistentQrCodeHash";

        Event retrievedEvent = scanQrViewModel.getEventByQrCodeHash(qrCodeHash).get();

        assertNull("Retrieved event should be null", retrievedEvent);
    }

    @Test(expected = NullPointerException.class)
    public void testGetEventByQrCodeHash_nullQrCodeHash() throws ExecutionException, InterruptedException {
        scanQrViewModel.getEventByQrCodeHash(null).get();
    }
}
