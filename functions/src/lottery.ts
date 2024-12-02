import { logger } from 'firebase-functions/v2';
import { AppEvent } from './types/app_event';
import { Signup } from './types/signup';
import { AppNotification } from './types/app_notification';

/**
 * Processes the lottery for a given event.
 * @param {FirebaseFirestore.Firestore} db Firestore instance
 * @param {string} eventId ID of the event
 * @param {AppEvent} eventData Data of the event
 * @param {number} numberOfEntrants Number of entrants to select
 */
export async function processLottery(
  db: FirebaseFirestore.Firestore,
  eventId: string,
  eventData: AppEvent,
  numberOfEntrants: number
) {
  logger.info(`Starting lottery for event: ${eventId}, ${eventData.eventName}`);

  const signupsSnapshot = await db
    .collection('signups')
    .where('eventId', '==', eventId)
    .where('cancelled', '==', false)
    .where('chosen', '==', false)
    .where('enrolled', '==', false)
    .get();

  if (signupsSnapshot.empty) {
    logger.warn(
      `No eligible signups found for event ${eventId}. Lottery skipped.`
    );
    return;
  }

  // Shuffle and select random entrants:
  const signups = signupsSnapshot.docs;
  const shuffledSignups = signups.sort(() => 0.5 - Math.random());
  const selectedSignups = shuffledSignups.slice(0, numberOfEntrants);

  const bulkWriter = db.bulkWriter();

  selectedSignups.forEach((signupDoc) => {
    const signupRef = signupDoc.ref;
    const signupData = signupDoc.data() as Signup;

    bulkWriter.update(signupRef, {
      isChosen: true,
    });

    const notificationRef = db.collection('notifications').doc();

    const notification: AppNotification = {
      userId: signupData.userId,
      eventId: eventId,
      title: `Invitation to ${eventData.eventName}`,
      message: `You have been selected to attend ${eventData.eventName}. Please confirm your attendance.`,
      type: 'Invite',
    };

    bulkWriter.create(notificationRef, notification);
  });

  await bulkWriter.close();
  logger.info(
    `Lottery processed. Selected ${selectedSignups.length} entrants for event ${eventId}.`
  );
}
