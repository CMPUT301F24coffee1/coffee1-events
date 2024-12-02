import { logger } from 'firebase-functions/v2';
import { AppEvent } from './types/app_event';
import { Signup } from './types/signup';
import { AppNotification } from './types/app_notification';
import { shuffleArray } from './utils';
import { messaging } from 'firebase-admin';

/**
 * Processes the lottery for a given event.
 * @param {FirebaseFirestore.Firestore} db Firestore instance
 * @param {string} eventId ID of the event
 * @param {AppEvent} eventData Data of the event
 * @param {number} numberOfEntrants Number of entrants to select
 * @return {string} String describing the result of the lottery
 */
export async function processLottery(
  db: FirebaseFirestore.Firestore,
  eventId: string,
  eventData: AppEvent,
  numberOfEntrants: number
): Promise<string> {
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
    const message =
      'Not enough eligible signups found for the event. Lottery skipped.';
    logger.debug(message);
    return message;
  }

  const { selectedSignups, lostSignups } = selectSignups(
    eligibleSignupsSnapshot.docs,
    numberOfEntrants,
    enrolledAmount,
    isReroll
  );

  await processSignups(db, eventId, eventData, selectedSignups, lostSignups);

  const message = `Lottery processed. Selected ${selectedSignups.length} entrants for the event.`;
  logger.info(message);
  return message;
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
  const notificationPromises: Promise<void>[] = [];

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

    notificationPromises.push(
      sendPushNotificationToUser(db, signupData.userId, notification)
    );
  });

  lostSignups.forEach((signupDoc) => {
    const signupRef = signupDoc.ref;
    const signupData = signupDoc.data() as Signup;

    bulkWriter.update(signupRef, {
      chosen: false,
      waitlisted: true,
      enrolled: false,
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
      title: `Information for "${eventData.eventName}"`,
      message:
        'Unfortunately, you have not been selected to attend the event. We are sorry.',
      type: 'General',
    };

    bulkWriter.create(notificationRef, notification);

    notificationPromises.push(
      sendPushNotificationToUser(db, signupData.userId, notification)
    );
  });

  await bulkWriter.close();
  await Promise.all(notificationPromises);
}

/**
 * Sends an android push notification to the selected user.
 * @param {FirebaseFirestore.Firestore} db Firestore instance.
 * @param {string} userId ID of the user.
 * @param {AppNotification} notificationData Data to send to the user.
 */
async function sendPushNotificationToUser(
  db: FirebaseFirestore.Firestore,
  userId: string,
  notificationData: AppNotification
) {
  try {
    const userDoc = await db.collection('users').doc(userId).get();

    if (!userDoc.exists) {
      console.log(`User ${userId} does not exist.`);
      return;
    }
    const userData = userDoc.data();
    const fcmToken = userData?.fcmToken;

    if (!fcmToken) {
      logger.warn(
        `No FCM token for user ${userId}. Cannot send push notification.`
      );
      return;
    }

    const message = {
      token: fcmToken,
      notification: {
        title: notificationData.title,
        body: notificationData.message,
      },
      data: {
        eventId: notificationData.eventId,
        type: notificationData.type,
      },
    };

    await messaging().send(message);
    console.log(`Successfully sent push notification to user ${userId}.`);
  } catch (error) {
    console.error(`Error sending push notification to user ${userId}:`, error);
  }
}
