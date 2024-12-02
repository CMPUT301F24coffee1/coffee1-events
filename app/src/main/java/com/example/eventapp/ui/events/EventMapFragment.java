package com.example.eventapp.ui.events;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.eventapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class EventMapFragment extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.maps_toolbar);
        toolbar.setTitle("Map");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert);

        // Handle Back Button Click
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mMap = googleMap;

        // Get data from Firestore
        db.collection("signups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean firstValidLocation = true;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");
                            String userId = document.getString("userId");

                            if (latitude != null && longitude != null) {
                                LatLng location = new LatLng(latitude, longitude);
                                mMap.addMarker(new MarkerOptions().position(location).title("User: " + userId));

                                // Set the camera to the first valid location
                                if (firstValidLocation) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10)); // Adjust zoom level as needed
                                    firstValidLocation = false; // After the first location, don't change the camera position
                                }
                            }
                        }
                    } else {
                        // Handle errors
                        Log.e("MapsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }
}
