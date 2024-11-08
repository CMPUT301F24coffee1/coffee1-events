package com.example.eventapp.services.photos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

/**
 * Helper class for handling photo-related permissions across different Android SDK versions.
 * <p>
 * This class manages permission requests and checks for photo access permissions,
 * taking into account Android's varying permission models across SDK versions.
 * For Android 13 and above, it requests `READ_MEDIA_IMAGES` permission for media images only;
 * for Android 6 to 12, it requests `READ_EXTERNAL_STORAGE`.
 * </p>
 */
public class PermissionHelper {

    /**
     * Makes sure the activity has the necessary permissions
     * depending on the user's SDK
     * Unused/not needed for now
     * @param activity the current activity
     * @param requestPermissionLauncher Launches permissions requester
     */
    public static void requestPhotoPermissions(Activity activity, ActivityResultLauncher<String[]> requestPermissionLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, request READ_MEDIA_IMAGES for images only
            if (!hasPhotoPermission(activity)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6 to Android 12, request READ_EXTERNAL_STORAGE
            if (!hasPhotoPermission(activity)) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        } else {
            // Permissions automatically granted on devices below Android 6
        }
    }

    // Check if photo permissions are granted
    public static boolean hasPhotoPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permissions automatically granted on devices below Android 6
    }
}

