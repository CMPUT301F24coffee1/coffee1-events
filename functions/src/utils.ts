/**
 * Delete all documents that fit a query.
 * @param {FirebaseFirestore.Query} query Query.
 * @return {Promise<number>} Promise - number of documents deleted.
 */
export async function deleteDocumentsByQuery(query: FirebaseFirestore.Query) {
  const querySnapshot = await query.get();

  if (querySnapshot.empty) {
    return 0;
  }
  const batch = query.firestore.batch();
  let count = 0;

  querySnapshot.docs.forEach((doc) => {
    batch.delete(doc.ref);
    count++;
  });

  await batch.commit();
  return count;
}

/**
 * Shuffles an array using the Fisher-Yates algorithm.
 * @param {T[]} array Array to shuffle
 * @return {T[]} Shuffled array
 */
export function shuffleArray<T>(array: T[]): T[] {
  const shuffled = array.slice();
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}
