package com.example.eventapp.photos;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

public class FirebaseStorageTest {

    private static final String TAG = "FirebaseStorageTest";

    // Method to test Firebase Storage upload
    public static void uploadTestFile(Context context) {
        // Create a Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("test/test_file.txt");

        // Create sample text data
        String testData = "This is a test file to check Firebase Storage setup.";
        byte[] data = testData.getBytes();

        // Start uploading data to Firebase Storage
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Upload successful!");
            // Optionally, get the download URL if you want to verify it
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "File available at: " + uri.toString());
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to retrieve download URL", e);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Upload failed", e);
        });
    }
}
