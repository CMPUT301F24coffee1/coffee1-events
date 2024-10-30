package com.example.eventapp.photos;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

public class PhotoPickerUtils {

    // Interface for callback to handle selected photo URI
    public interface PhotoPickerCallback {
        void onPhotoPicked(Uri photoUri);
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
                            callback.onPhotoPicked(photoUri);
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
}
