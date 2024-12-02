import { onCall, HttpsError } from 'firebase-functions/v2/https';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import * as logger from 'firebase-functions/logger';
import {
  onDocumentUpdated,
  onDocumentDeleted,
} from 'firebase-functions/v2/firestore';
import { User } from './types/user';
import { deleteDocumentsByQuery } from './utils';
import { AppEvent } from './types/app_event';
import { processLottery } from './lottery';

initializeApp();
const db = getFirestore();

/**
 * Processes an event lottery when called from the app by an organizer
 */
export const runLottery = onCall(async (request) => {
  const db = getFirestore();
  const eventId = request.data.eventId;
  const organizerId = request.data.organizerId;
  const numberOfAttendees = request.data.numberOfAttendees;

  if (!eventId || typeof eventId !== 'string') {
    throw new HttpsError(
      'invalid-argument',
      'The function must be called with a valid "eventId".'
    );
  }

  if (!numberOfAttendees || typeof numberOfAttendees !== 'number') {
    throw new HttpsError(
      'invalid-argument',
      'The function must be called with a valid "numberOfAttendees".'
    );
  }
  logger.info(`User ${organizerId} is running lottery for event: ${eventId}`);

  try {
    const eventSnapshot = await db.collection('events').doc(eventId).get();

    if (!eventSnapshot.exists) {
      throw new HttpsError(
        'not-found',
        `Event with ID ${eventId} does not exist.`
      );
    }
    const eventData = eventSnapshot.data() as AppEvent;

    if (eventData.organizerId !== organizerId) {
      throw new HttpsError(
        'permission-denied',
        'Only the organizer can run the lottery for this event.'
      );
    }

    await processLottery(db, eventId, eventData, numberOfAttendees);

    return { result: 'Lottery processed successfully.' };
  } catch (error) {
    logger.error(`Error processing lottery for event ${eventId}:`, error);
    throw new HttpsError('internal', `An error has occurred: ${error}`);
  }
});

/**
 * When a user removes their organizer status
 * Delete all of their associated facilities.
 */
export const handleOrganizerStatusChange = onDocumentUpdated(
  'users/{userId}',
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn('handleOrganizerStatusChange: No data found');
      return;
    }
    const dataBefore = snapshot.before.data() as User;
    const dataAfter = snapshot.after.data() as User;

    if (!(dataBefore?.organizer === true && dataAfter?.organizer === false)) {
      return;
    }
    const organizerId = event.params.userId;

    logger.debug(
      `User ${organizerId} is no longer an organizer. Deleting facilities...`
    );

    try {
      const facilitiesQuery = db
        .collection('facilities')
        .where('organizerId', '==', organizerId);

      const deletedCount = await deleteDocumentsByQuery(facilitiesQuery);
      logger.debug(
        `Deleted ${deletedCount} facilities for user ${organizerId}.`
      );
    } catch (error) {
      logger.error(
        `Failed to delete facilities for user ${organizerId}:`,
        error
      );
    }
  }
);

/**
 * When a user is deleted
 * Delete all signups and facilities associated with the user.
 */
export const handleUserDeleted = onDocumentDeleted(
  'users/{userId}',
  async (event) => {
    const userId = event.params.userId;

    logger.debug(
      `User ${userId} is being deleted. Deleting facilities and signups...`
    );

    const queries = [
      {
        name: 'facilities',
        query: db.collection('facilities').where('organizerId', '==', userId),
      },
      {
        name: 'signups',
        query: db.collection('signups').where('userId', '==', userId),
      },
    ];

    try {
      for (const { name, query } of queries) {
        const deletedCount = await deleteDocumentsByQuery(query);
        logger.debug(
          `Deleted ${deletedCount} ${name} documents for user ${userId}.`
        );
      }
    } catch (error) {
      logger.error(`Failed to delete documents for user ${userId}:`, error);
    }
  }
);

/**
 * When a facility is deleted
 * Delete all events associated with that facility.
 */
export const handleFacilityDeleted = onDocumentDeleted(
  'facilities/{facilityId}',
  async (event) => {
    const facilityId = event.params.facilityId;

    logger.debug(
      `Facility ${facilityId} deleted. Finding associated events...`
    );

    try {
      const eventsQuery = db
        .collection('events')
        .where('facilityId', '==', facilityId);

      const deletedCount = await deleteDocumentsByQuery(eventsQuery);
      logger.debug(
        `Deleted ${deletedCount} events for facility ${facilityId}.`
      );
    } catch (error) {
      logger.error(
        `Failed to delete events for facility ${facilityId}:`,
        error
      );
    }
  }
);

/**
 * When an event is deleted
 * Delete all signups associated with that event.
 */
export const handleEventDeleted = onDocumentDeleted(
  'events/{eventId}',
  async (event) => {
    const eventId = event.params.eventId;

    logger.debug(`Facility ${eventId} deleted. Deleting all signups...`);

    try {
      const signupsQuery = db
        .collection('signups')
        .where('eventId', '==', eventId);

      const deletedCount = await deleteDocumentsByQuery(signupsQuery);
      logger.debug(`Deleted ${deletedCount} signups for event ${eventId}.`);
    } catch (error) {
      logger.error(`Failed to delete signups for event ${eventId}:`, error);
    }
  }
);
