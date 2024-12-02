package com.example.eventapp.ui.events;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.Facility;
import com.example.eventapp.services.FormatDate;
import com.example.eventapp.viewmodels.EventsViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class CreateEventDatesFragment extends Fragment {
    private EventsViewModel eventsViewModel;
    private NavController navController;
    private ArrayList<Long> timestamps;
    private Event newEvent;

    private TextView eventDuration;
    private TextView eventRegistrationDeadline;

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(EventsViewModel.class);
        navController = NavHostFragment.findNavController(this);
        timestamps = new ArrayList<>(Arrays.asList(0L, 0L, 0L)); // (startTimestamp, endTimestamp, deadlineTimestamp)
    }

    /**
     * Inflates the layout for the fragment and initializes UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_event_dates, container, false);

        // Hide the profile button
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_nav_menu_event_dates, menu);
                menu.findItem(R.id.navigation_profile).setVisible(false); // Hide old menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_create_event_confirm) {
                    sendEventAndNavigate();
                    return true;
                }
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
        eventDuration = view.findViewById(R.id.create_event_dates_duration);
        eventRegistrationDeadline = view.findViewById(R.id.create_event_dates_deadline);
        Button dateButton = view.findViewById(R.id.create_event_dates_button);
        newEvent = eventsViewModel.getCreatingEvent();

        if (!eventsViewModel.isCreatingEventDatesInitialized()) {
            // automatically do date picker if it hasn't been done yet
            runDatePickers();
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(newEvent.getStartDate() + 86400000);
            String startTime = format.format(calendar.getTime());
            calendar.setTimeInMillis(newEvent.getEndDate() + 86400000);
            String endTime = format.format(calendar.getTime());
            eventDuration.setText(getString(
                    R.string.event_duration,
                    startTime,
                    endTime
            ));
            calendar.setTimeInMillis(newEvent.getDeadline() + 86400000);
            String deadLine = format.format(calendar.getTime());
            eventRegistrationDeadline.setText(getString(
                    R.string.registration_deadline,
                    deadLine)
            );
        }

        dateButton.setOnClickListener(v -> {
            runDatePickers();
        });

    }

    /**
     * Runs the date pickers, and then returns the timestamps of the dates selected
     * @return Start time, end time, and deadline of picked dates.
     */
    private void runDatePickers() {

        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Pair<Long, Long>> durationDatePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Registration Duration")
            .setCalendarConstraints(constraints.build())
            .build();

        AtomicReference<Pair<Long, Long>> durationDates = new AtomicReference<>();

        durationDatePicker.show(requireActivity().getSupportFragmentManager(), TAG);
        durationDatePicker.addOnPositiveButtonClickListener(dates -> {
            durationDates.set(dates);
            CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(durationDates.get().first);
            CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(durationDates.get().second);

            ArrayList<CalendarConstraints.DateValidator> listValidators =
                    new ArrayList<CalendarConstraints.DateValidator>();
            listValidators.add(dateValidatorMin);
            listValidators.add(dateValidatorMax);
            CalendarConstraints.Builder constraintsDeadline = new CalendarConstraints.Builder()
                    .setValidator(CompositeDateValidator.allOf(listValidators));

            MaterialDatePicker<Long> deadlineDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Deadline")
                    .setCalendarConstraints(constraintsDeadline.build())
                    .build();

            deadlineDatePicker.show(requireActivity().getSupportFragmentManager(), TAG);
            deadlineDatePicker.addOnPositiveButtonClickListener(date -> {
                eventsViewModel.setCreatingEventDatesInitialized(true);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(durationDates.get().first + 86400000);
                newEvent.setStartDate(calendar.getTimeInMillis());
                String startTime = format.format(calendar.getTime());
                calendar.setTimeInMillis(durationDates.get().second + 86400000);
                String endTime = format.format(calendar.getTime());
                newEvent.setEndDate(calendar.getTimeInMillis());
                eventDuration.setText(getString(
                        R.string.event_duration,
                        startTime,
                        endTime
                ));
                calendar.setTimeInMillis(date + 86400000);
                String deadLine = format.format(calendar.getTime());
                newEvent.setDeadline(calendar.getTimeInMillis());
                eventRegistrationDeadline.setText(getString(
                        R.string.registration_deadline,
                        deadLine)
                );

            });
        });
    }

    /**
     * Sets up an event to send to the view model, and then navigates to the next flow step
     */
    private void sendEventAndNavigate() {
        eventsViewModel.setCreatingEvent(newEvent);
        navController.navigate(R.id.navigation_create_event_confirm);
    }
}
