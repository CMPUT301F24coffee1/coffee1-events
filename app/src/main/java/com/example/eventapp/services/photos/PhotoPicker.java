package com.example.eventapp.services.photos;

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

/**
 * Utility class for selecting a photo from a user's photos and returning the photo's Uri.
 * Includes methods to open the photo picker and launch the ActivityResultLauncher
 */
public class PhotoPicker {

    /**
     * Interface that can be used to access the selected photo's Uri
     */
    public interface PhotoPickerCallback {
        void onPhotoPicked(Uri photoUri); // Called when a photo is selected
    }

    /**
     * Registers ActivityResultLauncher to start a photo picker intent and handle the result.
     * This launcher is intended for use in a Fragment and will invoke the provided callback
     * with the URI of the selected photo, if available.
     * @param fragment The fragment that the launcher is registered in.
     * @param callback If the photo selection is successful, this is used to handle the selected photo Uri
     * @return An ActivityResultLauncher that can be used to launch a photo picker intent
     */
    public static ActivityResultLauncher<Intent> getPhotoPickerLauncher(
            Fragment fragment,
            PhotoPickerCallback callback) {

        return fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri photoUri = result.getData().getData();
                        if (photoUri != null) {
                            callback.onPhotoPicked(photoUri); // Return the URI via callback
                        } else {
                            Log.d("PhotoPicker", "Photo URI is null");
                        }
                    } else {
                        Log.d("PhotoPicker", "Photo selection failed");
                    }
                });
    }

    // Method to open the photo picker
    public static void openPhotoPicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }
}
