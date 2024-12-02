package com.example.eventapp.services.photos;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.OutputStream;

public class QRCodeSaver {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private Context context;

    public QRCodeSaver(Context context) {
        this.context = context;
    }

    public void saveQRCodeToGallery(Bitmap bitmap, String fileName) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            saveBitmapToGallery(bitmap, fileName);
        }
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QR Codes");
        }

        OutputStream outputStream = null;
        try {
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imageUri == null) {
                throw new Exception("Failed to create new MediaStore record.");
            }
            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream == null) {
                throw new Exception("Failed to open output stream.");
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(context, "QR Code saved to gallery!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("QRCodeSaver", "Error saving QR Code", e);
            Toast.makeText(context, "Failed to save QR Code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Log.e("QRCodeSaver", "Error closing output stream", e);
                }
            }
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE);
    }

    private void requestStoragePermission(Fragment fragment) {
        fragment.requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE
        );
    }
}
