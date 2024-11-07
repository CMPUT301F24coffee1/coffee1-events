package com.example.eventapp.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class PhotoUploader {

    // Interface for callback to handle upload success and failure
    public interface UploadCallback {
        void onUploadSuccess(String downloadUrl); // Called when upload is successful
        void onUploadFailure(Exception e);        // Called when upload fails
    }

    // Method to compress and upload the image
    public static void uploadPhotoToFirebase(Context context, Uri photoUri, int quality, String pathPrefix, String id, String title, UploadCallback callback) {
        String imagePath = pathPrefix + "/" + id + "/" + title + ".jpg";
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

    // Version that doesn't specify id, so generates one
    public static void uploadPhotoToFirebase(Context context, Uri photoUri, int quality, String path, String title, UploadCallback callback) {
        String uniqueImageId = UUID.randomUUID().toString();
        uploadPhotoToFirebase(context, photoUri, quality, path,title, uniqueImageId, callback);
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
}
