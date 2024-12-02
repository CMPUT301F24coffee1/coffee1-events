package com.example.eventapp.ui.events;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.repositories.FacilityRepository;
import com.example.eventapp.services.photos.PhotoManager;
import com.example.eventapp.ui.images.ImageInfoFragment;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.example.eventapp.viewmodels.ImagesViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CreateEventConfirmFragment extends Fragment {
    private EventsViewModel eventsViewModel;
    private NavController navController;
    private boolean eventSubmittable;
    private Event newEvent;
    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        navController = NavHostFragment.findNavController(this);
    }

    /**
     * Inflates the layout for the fragment and initializes UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_event_confirm, container, false);

        // Hide the profile button
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                return false;
            }
        }, getViewLifecycleOwner());

        initViewComponents(view);

        return view;
    }

    /**
     * Initializes UI components by finding views by their IDs.
     *
     * @param view the root view of the fragment's layout.
     */
    private void initViewComponents(View view) {
        eventSubmittable = true;

        TextView eventName = view.findViewById(R.id.create_event_card_event_name);
        TextView eventDescription = view.findViewById(R.id.create_event_card_description);
        TextView geolocationRequired = view.findViewById(R.id.create_event_card_geolocation);
        TextView maxEventEntrants = view.findViewById(R.id.create_event_card_entrants);
        ImageView posterImageView = view.findViewById(R.id.create_event_card_image);
        CardView facilityImageCard = view.findViewById(R.id.create_event_card_facility_image_card);
        ImageView facilityImage = view.findViewById(R.id.create_event_card_facility_image);
        TextView facilityText = view.findViewById(R.id.create_event_card_facility_name);
        TextView numberOfAttendees = view.findViewById(R.id.create_event_card_attendees);
        Button createButton = view.findViewById(R.id.create_event_card_confirm);
        TextView registrationDuration = view.findViewById(R.id.create_event_card_registration_duration);
        TextView registrationDeadline = view.findViewById(R.id.create_event_card_registration_deadline);

        newEvent = eventsViewModel.getCreatingEvent();
        eventName.setText(newEvent.getEventName());
        eventDescription.setText(newEvent.getEventDescription());
        geolocationRequired.setText(newEvent.isGeolocationRequired() ? "Geolocation Required" : "Geolocation Not Required");
        maxEventEntrants.setText(newEvent.getMaxEntrants() != -1 ? "Waitlist Size: " + String.valueOf(newEvent.getMaxEntrants()): "");
        numberOfAttendees.setText("Number of Attendees: " + String.valueOf(newEvent.getNumberOfAttendees()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(newEvent.getStartDate());
        String startTime = format.format(calendar.getTime());
        calendar.setTimeInMillis(newEvent.getEndDate());
        String endTime = format.format(calendar.getTime());
        registrationDuration.setText(getString(
                R.string.event_duration,
                startTime,
                endTime
        ));
        calendar.setTimeInMillis(newEvent.getDeadline());
        String deadLine = format.format(calendar.getTime());
        registrationDeadline.setText(getString(
                R.string.registration_deadline,
                deadLine)
        );
        Glide.with(requireContext())
                .load(newEvent.getPosterUri())
                .into(posterImageView);

        FacilityRepository facilityRepository = FacilityRepository.getInstance();
        facilityRepository.getFacilityById(newEvent.getFacilityId()).thenAccept(facility -> {
            if (facility.hasPhoto()) {
                Glide.with(requireContext())
                        .load(facility.getPhotoUri())
                        .into(facilityImage);
            }
            facilityText.setText(facility.getFacilityName());
            facilityImageCard.setOnClickListener(v -> {
                if (v.isClickable()) {
                    ImagesViewModel imagesViewModel = new ViewModelProvider(requireActivity()).get(ImagesViewModel.class);
                    imagesViewModel.setSelectedImage(facility.getPhotoUri());
                    new ImageInfoFragment().show(requireActivity().getSupportFragmentManager(), "fragment_image_info");
                }
            });
        });

        createButton.setOnClickListener(v -> {
            if (eventSubmittable) {
                createEvent();
            }
        });

    }

    /**
     * Creates the event, uploads it to the database, and then dismisses the fragment
     */
    private void createEvent() {
        eventSubmittable = false;
        if (newEvent.getPosterUriString() == null) {
            eventsViewModel.addEvent(newEvent);
            navController.navigate(R.id.navigation_events);
        } else {
            uploadPhotoAndCreateEvent(newEvent);
        }
    }

    /**
      * Uploads the selected photo to Firebase Storage and submits the event, then navigates, upon success
      *
      * @param event the Event object to be created after the photo upload.
      */
    private void uploadPhotoAndCreateEvent(Event event) {
        Fragment fragment = this;
        PhotoManager.uploadPhotoToFirebase(
                getContext(),
                Uri.parse(event.getPosterUriString()),
                75,
                "events",
                "poster",
                new PhotoManager.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        String uriString = downloadUrl;
                        Log.d("PhotoUploader", "Photo uploaded successfully: " + uriString);
                        event.setPosterUriString(downloadUrl);
                        eventsViewModel.addEvent(event);
                        navController.popBackStack(R.id.navigation_create_event, true);
                    }

                    @Override
                    public void onUploadFailure(Exception e) {
                        Log.e("PhotoUploader", "Upload failed", e);
                        Toast.makeText(getContext(), "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        eventSubmittable = true;
                    }
                });
    }
}
