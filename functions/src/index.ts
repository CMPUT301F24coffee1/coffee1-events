import { onTaskDispatched } from 'firebase-functions/v2/tasks';
import { getFunctions } from 'firebase-admin/functions';
import { onCall, HttpsError } from 'firebase-functions/v2/https';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import * as logger from 'firebase-functions/logger';
import {
  onDocumentCreated,
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
 * Processes an event lottery when called from the app by an organizer.
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
  if (!organizerId || typeof organizerId !== 'string') {
    throw new HttpsError(
      'invalid-argument',
      'The function must be called with a valid "organizerId".'
    );
  }
  if (
    !numberOfAttendees ||
    typeof numberOfAttendees !== 'number' ||
    numberOfAttendees < 1
  ) {
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
 * Task Queue Function: Processes the lottery for a given event after the deadline passes.
 */
export const runLotteryByQueue = onTaskDispatched(
  {
    retryConfig: {
      maxAttempts: 3,
      minBackoffSeconds: 60,
    },
    rateLimits: {
      maxConcurrentDispatches: 3,
    },
  },
  async (req) => {
    const eventId = req.data.eventId;

    if (!eventId) {
      logger.error('No eventId provided for lottery processing');
      throw new Error('Missing eventId');
    }
    logger.info(`Processing lottery for event: ${eventId}`);

    try {
      const snapshot = await db.collection('events').doc(eventId).get();

      if (!snapshot.exists) {
        logger.error(`Event with ID ${eventId} does not exist.`);
        return;
      }
      const eventData = snapshot.data() as AppEvent;

      if (!eventData) {
        logger.error(`Event data is null for ID ${eventId}`);
        return;
      }
      if (eventData.lotteryProcessed == true) {
        logger.info(`Lottery already processed for event: ${eventId}.`);
        return;
      }

      await processLottery(db, eventId, eventData, eventData.numberOfAttendees);

      logger.debug(`Lottery processed successfully for event: ${eventId}`);
    } catch (error) {
      logger.error(`Error processing lottery for event ${eventId}:`, error);
      throw error;
    }
  }
);

/**
 * When an event is created
 * Enroll it into the lottery system.
 */
export const handleEventCreated = onDocumentCreated(
  'events/{eventId}',
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn('handleEventCreated: No data found');
      return;
    }
    const eventId = event.params.eventId;
    const data = snapshot.data() as AppEvent;
    const deadlineTimestamp = data.deadline;

    logger.debug(`Enrolling event with id ${eventId} into the lottery system`);
    const queue = getFunctions().taskQueue('processLottery');

    try {
      await queue.enqueue(
        { eventId },
        {
          scheduleDelaySeconds: Math.max(
            0,
            Math.floor(deadlineTimestamp / 1000) - Math.floor(Date.now() / 1000)
          ),
        }
      );

      logger.info(`Lottery task scheduled for event: ${eventId}`);
    } catch (error) {
      logger.error(
        `Failed to schedule lottery task for event ${eventId}:`,
        error
      );
    }
  }
);

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
      {
        name: 'notifications',
        query: db
          .collection('users')
          .doc(userId)
          .collection('notifications')
          .where('userId', '==', userId),
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
