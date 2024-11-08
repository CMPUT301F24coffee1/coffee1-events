package com.example.eventapp.services.photos;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * The PhotoManager class provides utility methods for handling photo operations, including
 * compression, uploading to Firebase Storage, deletion, and generating default profile pictures.
 * It includes a callback interface for upload status, allowing the calling component to respond
 * to upload success or failure events. This class also contains helper methods to generate initials
 * and color-based default profile images for users without custom profile pictures.
 */
public class PhotoManager {


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
        uploadPhotoToFirebase(context, photoUri, quality, path, uniqueImageId, title, callback);
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

    /**
     * Deletes a photo from the firebase storage
     * @param photoUri The URI of the photo to be deleted
     */
    public static void deletePhotoFromFirebase(Uri photoUri) {
        if (photoUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(Objects.requireNonNull(photoUri.getLastPathSegment()));
            storageRef.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "deletePhotoFromFirebase: success - URI: " + photoUri);
                        } else {
                            Log.e(TAG, "deletePhotoFromFirebase: fail", task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "photoUri is null, cannot delete photo from firebase");
        }

    }

    /**
     * Generates a default profile picture (for people who do not upload a custom one)
     * @param userName the name of the user whose profile picture is to be generated
     * @param userId the ID of the user whose profile picture is to be generated
     * @return a bitmap containing the generated profile picture
     */
    public static Bitmap generateDefaultProfilePicture(String userName, String userId) {

        String nameAndIdHash = userName + userId;
        String initials = getInitials(userName);
        int color = getColorFromName(nameAndIdHash);


        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(100, 100, 100, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(initials, 100, 120, paint);

        return bitmap;
    }

    /**
     * Retrieves the initials of an input name
     * @param name the name for the initials to be parsed from
     * @return a string containing just the initials of the input name
     */
    private static String getInitials(String name) {
        if (name.isEmpty()) {
            return name;
        }
        String[] words = name.split(" ");
        if (words.length < 2) return String.format("%c", name.charAt(0)).toUpperCase();
        return (String.format("%c%c", words[0].charAt(0), words[1].charAt(0))).toUpperCase();
    }

    /**
     * Generates a hash code for an input name and returns a colour depending on the hash
     * @param name the name to generate a hash from
     * @return the colour corresponding to the hash of the name
     */
    private static int getColorFromName(String name) {
        int hash = name.hashCode();
        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA};
        return colors[Math.abs(hash) % colors.length];
    }
}
