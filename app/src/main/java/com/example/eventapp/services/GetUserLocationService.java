package com.example.eventapp.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GetUserLocationService {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    // Callback interface to return location data
    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    public GetUserLocationService(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void fetchUserLocation(Activity activity, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request missing permissions
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return;
        }

        // Permissions are granted; fetch the user's location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        callback.onLocationReceived(location);
                    } else {
                        Toast.makeText(context, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
