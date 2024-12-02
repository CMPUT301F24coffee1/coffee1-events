package com.example.eventapp.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveImageService extends Service {

    private static final String TAG = "SaveImageService";
    public static final String EXTRA_BITMAP = "extra_bitmap";
    public static final String EXTRA_FILENAME = "extra_filename";

    private Handler serviceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("SaveImageThread");
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceHandler.post(() -> handleIntent(intent));
        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Bitmap bitmap = intent.getParcelableExtra(EXTRA_BITMAP);
            String filename = intent.getStringExtra(EXTRA_FILENAME);

            if (bitmap != null && filename != null) {
                saveImageToGallery(bitmap, filename);
            }
        }
    }

    private void saveImageToGallery(Bitmap bitmap, String filename) {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(picturesDir, "QRImages");
        if (!appDir.exists() && !appDir.mkdirs()) {
            Log.e(TAG, "Failed to create directory.");
            return;
        }

        File file = new File(appDir, filename + ".png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.d(TAG, "Image saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a started service, not a bound service, so return null.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceHandler != null) {
            serviceHandler.getLooper().quitSafely();
        }
    }
}
