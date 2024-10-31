package com.example.eventapp.photos;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

public class DefaultImageUploader {

    public void uploadImageAndSaveUri() {
        String imageUrl = "https://qwestore.com/loading/1280-853.png";

        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("events/poster_default.png");

        // Firestore database reference
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Background thread for network operation
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                // Upload to Firebase Storage
                UploadTask uploadTask = storageRef.putStream(inputStream);
                uploadTask.addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUri = uri.toString();
                            // Save URI to Firestore
                            firestore.collection("settings")
                                    .document("defaultPoster")
                                    .set(new DefaultPoster(downloadUri))
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("Firestore", "Default poster URI saved to Firestore: " + downloadUri);
                                        } else {
                                            Log.e("Firestore", "Failed to save URI to Firestore", task.getException());
                                        }
                                    });
                        }).addOnFailureListener(exception -> {
                            Log.e("FirebaseStorage", "Error getting download URI", exception);
                        })
                ).addOnFailureListener(exception -> {
                    Log.e("FirebaseStorage", "Image upload failed", exception);
                });
            } catch (Exception e) {
                Log.e("FirebaseStorage", "Error uploading image", e);
            }
        }).start();
    }

    // Helper class to represent the URI
    public static class DefaultPoster {
        private String uri;

        public DefaultPoster() {} // Needed for Firestore

        public DefaultPoster(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }
}

