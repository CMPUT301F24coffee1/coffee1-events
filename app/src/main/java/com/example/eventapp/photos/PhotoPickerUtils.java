package com.example.eventapp.photos;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhotoPickerUtils {

    // Interface for callback to handle the selected photo URI and download URL
    public interface PhotoPickerCallback {
        void onPhotoUploadComplete(String downloadUrl); // Called when photo is uploaded to Firebase
        void onPhotoUploadFailed(Exception e);    // Called if upload fails
    }

    // Method to create a photo picker launcher, designed for fragments
    public static ActivityResultLauncher<Intent> getPhotoPickerLauncher(
            Fragment fragment,
            PhotoPickerCallback callback) {

        return fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri photoUri = result.getData().getData();
                        if (photoUri != null) {
                            uploadPhotoToFirebase(photoUri, callback); // Upload photo to Firebase
                        }
                    }
                });
    }

    // Method to open the photo picker
    public static void openPhotoPicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    // Method to upload photo to Firebase Storage and save download URL to Firestore
    private static void uploadPhotoToFirebase(Uri photoUri, PhotoPickerCallback callback) {
        // Generate a unique filename for the image
        String uniqueImageId = UUID.randomUUID().toString();
        String imagePath = "events/" + uniqueImageId + "/poster.jpg";

        // Reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

        // Upload file to Firebase Storage
        storageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL once upload completes
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Save download URL to Firestore
                        saveDownloadUrlToFirestore(uniqueImageId, downloadUrl, callback);

                    }).addOnFailureListener(callback::onPhotoUploadFailed); // Handle error in callback
                })
                .addOnFailureListener(callback::onPhotoUploadFailed); // Handle upload failure
    }

    // Method to save download URL to Firestore
    private static void saveDownloadUrlToFirestore(String uniqueImageId, String downloadUrl, PhotoPickerCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Prepare data to save
        Map<String, Object> data = new HashMap<>();
        data.put("downloadUrl", downloadUrl);

        // Save the download URL to Firestore under a unique document ID
        firestore.collection("photos")
                .document(uniqueImageId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    callback.onPhotoUploadComplete(downloadUrl); // Notify success
                })
                .addOnFailureListener(callback::onPhotoUploadFailed); // Handle Firestore save failure
    }
}
