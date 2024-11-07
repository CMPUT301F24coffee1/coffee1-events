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

public class PhotoPicker {

    // Interface for callback to handle the selected photo URI
    public interface PhotoPickerCallback {
        void onPhotoPicked(Uri photoUri); // Called when a photo is selected
    }

    // Method to create a photo picker launcher, designed for fragments
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
