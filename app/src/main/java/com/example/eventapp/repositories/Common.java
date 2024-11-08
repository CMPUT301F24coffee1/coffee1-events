package com.example.eventapp.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eventapp.interfaces.HasDocumentId;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Common utility class providing shared methods for Firestore data handling.
 *
 * This class includes methods to execute Firestore queries and convert query results into
 * LiveData for easy observation in the application. Additionally, it supports parsing Firestore
 * documents into specific model objects, with document IDs automatically assigned when applicable.
 */
public class Common {

    /**
     * Runs a Firestore query and returns the results as LiveData.
     *
     * @param methodName The name of the method calling this function, used for logging.
     * @param query The Firestore query to run.
     * @param clazz The class type of the documents being queried.
     * @param <T> The type of documents being queried.
     * @return LiveData containing a list of queried documents of type T.
     */
    public static <T> LiveData<List<T>> runQueryLiveData(String methodName, Query query, Class<T> clazz, String tag) {
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(tag, "runQueryLiveData: " + methodName + ": listen failed", e);
                liveData.setValue(new ArrayList<>());
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<T> items = parseDocuments(querySnapshot, clazz);
                Log.d(tag, "runQueryLiveData: " + methodName + ": success, retrieved " + items.size() + " items");
                liveData.setValue(items);
            } else {
                Log.d(tag, "runQueryLiveData: " + methodName + ": no documents found");
                liveData.setValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    /**
     * Parses a QuerySnapshot into a list of objects of type T, setting document IDs if applicable.
     *
     * @param querySnapshot The QuerySnapshot containing documents to parse.
     * @param clazz The class type of the documents being parsed.
     * @param <T> The type of documents being parsed.
     * @return A list of objects of type T parsed from the QuerySnapshot.
     */
    private static <T> List<T> parseDocuments(QuerySnapshot querySnapshot, Class<T> clazz) {
        List<T> items = querySnapshot.toObjects(clazz);
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            if (item instanceof HasDocumentId) {
                ((HasDocumentId) item).setDocumentId(querySnapshot.getDocuments().get(i).getId());
            }
        }
        return items;
    }
}
