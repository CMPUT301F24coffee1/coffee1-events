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

/**
 * Service class for getting the users location
 * Has various checks and calls to make sure the user's location is accessible
 */
public class GetUserLocationService {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    /**
     * Callback interface to return location data
     */
    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    /**
     * This method initializes the service with context
     * @param context The current context from the activity
     */
    public GetUserLocationService(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * This method checks the users current location permissions
     * If we do not yet have the user permissions it requests them and returns
     * If we do have the permissions, then it returns the users location
     * @param activity The current activity
     * @param callback A callback to put the location info into
     */
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
