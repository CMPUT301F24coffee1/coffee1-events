package com.example.eventapp.ui.events;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * DatePickerFragment is a dialog fragment that displays a date picker to the user. It allows for
 * selecting a date, which is then returned as a Unix timestamp in seconds via a callback listener.
 * This fragment can be used to set dates for different purposes, identified by the provided type.
 *
 * <p>Implements {@link DatePickerDialog.OnDateSetListener} to handle the date selection event
 * from the date picker dialog.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DatePickerFragment datePicker = new DatePickerFragment(listener, type);
 * datePicker.show(fragmentManager, "datePicker");
 * }</pre>
 *
 * <p>Reference: <a href="https://developer.android.com/develop/ui/views/components/pickers">
 * Android Developer Docs - Pickers</a></p>
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private Calendar calendar;
    private SetDateListener listener;
    private int type;

    interface SetDateListener {
        void setDate(long timestamp, int type);
    }

    public DatePickerFragment(SetDateListener listener, int type){
        this.listener = listener;
        this.type = type;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day){
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        long timestamp = calendar.getTimeInMillis() / 1000;
        listener.setDate(timestamp, type);
    }
}
