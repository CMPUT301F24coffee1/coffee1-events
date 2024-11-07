package com.example.eventapp.photos;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for uploading photos to Firebase Storage.
 * This class includes methods for compressing an image and uploading it to Firebase.
 */
public class PhotoUploader {

    /**
     * Interface for a callback to handle upload success and failure.
     * Used to provide feedback on the status of the photo upload process.
     */
    public interface UploadCallback {
        void onUploadSuccess(String downloadUrl); // Called when upload is successful
        void onUploadFailure(Exception e);        // Called when upload fails
    }

    /**
     * Compresses and uploads photo to firebase on a unique path given a photo Uri.
     * On success, it calls the callback with the download URL of the uploaded image.
     * @param context  The application context.
     * @param photoUri The URI of the photo to be uploaded.
     * @param quality  The quality level for compression (0-100).
     * @param callback The callback interface to handle success or failure of the upload.
     */
    public static void uploadPhotoToFirebase(Context context, Uri photoUri, int quality, UploadCallback callback) {
        String uniqueImageId = UUID.randomUUID().toString();
        String imagePath = "events/" + uniqueImageId + "/poster.jpg";
        Log.d("PhotoUploader", "Image Path to upload: " + imagePath);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

        // Compress the image
        byte[] compressedImageData = compressImage(context, photoUri, quality);

        if (compressedImageData != null) {
            // Upload the compressed image data to Firebase Storage
            storageRef.putBytes(compressedImageData)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onUploadSuccess(uri.toString()); // Return download URL
                        }).addOnFailureListener(callback::onUploadFailure);
                    })
                    .addOnFailureListener(callback::onUploadFailure);
        } else {
            callback.onUploadFailure(new Exception("Image compression failed"));
        }
    }

    /**
     * Compresses an image from the given URI to a JPEG format byte array.
     * The compression quality can be adjusted to control the image size.
     *
     * @param context  The application context.
     * @param imageUri The URI of the image to compress.
     * @param quality  The quality level for compression (0-100).
     * @return A byte array containing the compressed image data, or null if compression fails.
     */
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
}
