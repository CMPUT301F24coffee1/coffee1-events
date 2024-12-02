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
