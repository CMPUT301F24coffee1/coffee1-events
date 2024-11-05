package com.example.eventapp.photos;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
                            uploadPhotoToFirebase(fragment.getContext(), photoUri, 75, callback); // Upload photo to Firebase
                        } else {
                            Log.d("PhotoPicker", "getPhotoPickerLauncher: Photo URI is null");
                        }
                    } else {
                        Log.d("PhotoPicker", "getPhotoPickerLauncher: result failed");
                    }
                });
    }

    // Method to open the photo picker
    public static void openPhotoPicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    // Compress the image from Uri
    private static byte[] compressImage(Context context, Uri imageUri, int quality) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Upload compressed photo to Firebase Storage
    private static void uploadPhotoToFirebase(Context context, Uri photoUri, int quality, PhotoPickerCallback callback) {
        String uniqueImageId = UUID.randomUUID().toString();
        String imagePath = "events/" + uniqueImageId + "/poster.jpg";
        Log.d("PhotoPickerUtils", "Image Path to upload: " + imagePath);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

        // Compress the image
        byte[] compressedImageData = compressImage(context, photoUri, quality);

        if (compressedImageData != null) {
            // Upload the compressed image data to Firebase Storage
            storageRef.putBytes(compressedImageData)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveDownloadUrlToFirestore(uniqueImageId, uri.toString(), callback);
                        }).addOnFailureListener(callback::onPhotoUploadFailed);
                    })
                    .addOnFailureListener(callback::onPhotoUploadFailed);
        } else {
            callback.onPhotoUploadFailed(new Exception("Image compression failed"));
        }
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
