import { getFirestore } from 'firebase-admin/firestore';
import { logger } from 'firebase-functions/v2';
import { AppEvent } from './types/app_event';

const db = getFirestore();

// TODO:
// Choosing [maximum number of entrants] winners
// Notifying the organizer of the result by creating a Notification object
// Notifying entrants of results by creating Notification objects
// Consider: BulkWriter

/**
 * Runs the lottery for a given event.
 * @param {string} eventId The ID of the event.
 * @param {AppData} eventData Data associated with the event.
 */
export async function runLottery(eventId: string, eventData: AppEvent) {
  logger.info(`Starting lottery for event: ${eventId}, ${eventData.eventName}`);

  const signupsSnapshot = await db
    .collection('signups')
    .where('eventId', '==', eventId)
    .get();

  if (signupsSnapshot.empty) {
    logger.warn(`No signups found for event ${eventId}. Lottery skipped.`);
    return;
  }
  logger.info(
    `Signups retrieved for event ${eventId}: Lottery not implemented, skipping.`
  );

  // const signups = signupsSnapshot.docs.map((doc) => doc.id);
  // const maximumEntrantsAllowed = eventData.maxEntrants;
}
