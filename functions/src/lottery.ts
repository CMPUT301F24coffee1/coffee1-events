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

  const [eligibleSignupsSnapshot, enrolledSignupsSnapshot] = await Promise.all([
    fetchEligibleSignups(db, eventId),
    fetchEnrolledSignups(db, eventId),
  ]);

  const isReroll = !enrolledSignupsSnapshot.empty;
  const enrolledAmount = enrolledSignupsSnapshot.size;
  const eligibleAmount = eligibleSignupsSnapshot.size;

  if (
    eligibleSignupsSnapshot.empty ||
    (isReroll && enrolledAmount + eligibleAmount < numberOfEntrants)
  ) {
    logger.warn(
      `Not enough eligible signups found for event ${eventId}. Lottery skipped.`
    );
    return;
  }

  const { selectedSignups, lostSignups } = selectSignups(
    eligibleSignupsSnapshot.docs,
    numberOfEntrants,
    enrolledAmount,
    isReroll
  );

  await processSignups(db, eventId, eventData, selectedSignups, lostSignups);

  logger.info(
    `Lottery processed. Selected ${selectedSignups.length} entrants for event ${eventId}.`
  );
}

/**
 * Fetches eligible signups for the lottery.
 * @param {FirebaseFirestore.Firestore} db Firestore instance
 * @param {string} eventId ID of the event
 * @return {Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>
 * } Promise resolving to QuerySnapshot of eligible signups
 */
async function fetchEligibleSignups(
  db: FirebaseFirestore.Firestore,
  eventId: string
): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
  return db
    .collection('signups')
    .where('eventId', '==', eventId)
    .where('cancelled', '==', false)
    .where('chosen', '==', false)
    .where('enrolled', '==', false)
    .where('waitlisted', '==', true)
    .get();
}

/**
 * Fetches currently enrolled signups for the lottery.
 * @param {FirebaseFirestore.Firestore} db Firestore instance
 * @param {string} eventId ID of the event
 * @return {Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>
 * } Promise resolving to QuerySnapshot of currently enrolled signups
 */
async function fetchEnrolledSignups(
  db: FirebaseFirestore.Firestore,
  eventId: string
): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
  return db
    .collection('signups')
    .where('eventId', '==', eventId)
    .where('enrolled', '==', true)
    .get();
}

/**
 * Selects signups for the lottery.
 * @param {FirebaseFirestore.QueryDocumentSnapshot[]} signups Array of signup documents
 * @param {number} numberOfEntrants Number of entrants to select
 * @param {number} enrolledAmount Number of already enrolled entrants
 * @param {boolean} isReroll Indicates if this is a reroll
 * @return {{
 * selectedSignups: FirebaseFirestore.QueryDocumentSnapshot[],
 * lostSignups: FirebaseFirestore.QueryDocumentSnapshot[] }}
 *         Object containing selected and lost signups
 */
function selectSignups(
  signups: FirebaseFirestore.QueryDocumentSnapshot[],
  numberOfEntrants: number,
  enrolledAmount: number,
  isReroll: boolean
): {
  selectedSignups: FirebaseFirestore.QueryDocumentSnapshot[];
  lostSignups: FirebaseFirestore.QueryDocumentSnapshot[];
} {
  const shuffledSignups = shuffleArray(signups);
  const slotsAvailable = isReroll
    ? numberOfEntrants - enrolledAmount
    : numberOfEntrants;

  const selectedSignups = shuffledSignups.slice(0, slotsAvailable);
  const lostSignups = shuffledSignups.slice(slotsAvailable);

  return { selectedSignups, lostSignups };
}

/**
 * Shuffles an array using the Fisher-Yates algorithm.
 * @param {T[]} array Array to shuffle
 * @return {T[]} Shuffled array
 */
function shuffleArray<T>(array: T[]): T[] {
  const shuffled = array.slice();
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

/**
 * Processes the selected and lost signups by updating their status and sending notifications.
 * @param {FirebaseFirestore.Firestore} db Firestore instance
 * @param {string} eventId ID of the event
 * @param {AppEvent} eventData Data of the event
 * @param {FirebaseFirestore.QueryDocumentSnapshot[]} selectedSignups Array of selected signup documents
 * @param {FirebaseFirestore.QueryDocumentSnapshot[]} lostSignups Array of lost signup documents
 */
async function processSignups(
  db: FirebaseFirestore.Firestore,
  eventId: string,
  eventData: AppEvent,
  selectedSignups: FirebaseFirestore.QueryDocumentSnapshot[],
  lostSignups: FirebaseFirestore.QueryDocumentSnapshot[]
) {
  const bulkWriter = db.bulkWriter();

  selectedSignups.forEach((signupDoc) => {
    const signupRef = signupDoc.ref;
    const signupData = signupDoc.data() as Signup;

    bulkWriter.update(signupRef, {
      chosen: true,
      waitlisted: false,
      cancelled: false,
    });

    const notificationRef = db
      .collection('users')
      .doc(signupData.userId)
      .collection('notifications')
      .doc();

    const notification: AppNotification = {
      userId: signupData.userId,
      eventId: eventId,
      title: `Invitation to ${eventData.eventName}`,
      message: `You have been selected to attend ${eventData.eventName}. Please confirm your attendance.`,
      type: 'Invite',
    };

    bulkWriter.create(notificationRef, notification);
  });

  lostSignups.forEach((signupDoc) => {
    const signupRef = signupDoc.ref;
    const signupData = signupDoc.data() as Signup;

    bulkWriter.update(signupRef, {
      chosen: false,
      waitlisted: false,
      enrolled: false,
      cancelled: true,
    });

    const notificationRef = db
      .collection('users')
      .doc(signupData.userId)
      .collection('notifications')
      .doc();

    const notification: AppNotification = {
      userId: signupData.userId,
      eventId: eventId,
      title: `Information for "${eventData.eventName}"`,
      message:
        'Unfortunately, you have not been selected to attend the event. We are sorry.',
      type: 'General',
    };

    bulkWriter.create(notificationRef, notification);
  });

  await bulkWriter.close();
}
