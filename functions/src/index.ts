import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import * as logger from 'firebase-functions/logger';
import {
  onDocumentUpdated,
  onDocumentDeleted,
} from 'firebase-functions/v2/firestore';
import { User } from './types/user';
import { deleteDocumentsByQuery } from './utils';

initializeApp();
const db = getFirestore();

/**
 * When a user removes their organizer status
 * Remove all of their associated facilities.
 */
export const handleOrganizerStatusChange = onDocumentUpdated(
  'users/{userId}',
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn('No data associated with the event');
      return;
    }
    const dataBefore = snapshot.before.data() as User;
    const dataAfter = snapshot.after.data() as User;

    if (!(dataBefore?.organizer === true && dataAfter?.organizer === false)) {
      return;
    }
    const organizerId = dataBefore.userId;

    logger.debug(
      `User ${organizerId} is no longer an organizer. Deleting facilities...`
    );

    try {
      const facilitiesQuery = db
        .collection('facilities')
        .where('organizerId', '==', organizerId);

      return deleteDocumentsByQuery(facilitiesQuery).then((deletedCount) => {
        logger.debug(
          `Deleted ${deletedCount} facilities for user ${organizerId}.`
        );
      });
    } catch (error) {
      logger.error(
        `Failed to delete facilities for user ${organizerId}:`,
        error
      );
    }
  }
);

/**
 * When a facility is removed
 * Remove all events associated with that facility.
 */
export const handleFacilityDeleted = onDocumentDeleted(
  'facilities/{facilityId}',
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) {
      logger.warn('No data associated with the event');
      return;
    }

    const facilityId = event.params.facilityId;

    logger.debug(
      `Facility ${facilityId} deleted. Finding associated events...`
    );

    try {
      const eventsQuery = db
        .collection('events')
        .where('facilityId', '==', facilityId);

      return deleteDocumentsByQuery(eventsQuery).then((deletedCount) => {
        logger.debug(
          `Deleted ${deletedCount} events for facility ${facilityId}.`
        );
      });
    } catch (error) {
      logger.error(
        `Failed to delete events for facility ${facilityId}:`,
        error
      );
    }
  }
);
