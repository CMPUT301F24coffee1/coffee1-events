package com.example.eventapp.ui.events;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to format the dates given in long to dates given in a simpler string format
 */
public class FormatDate {

    /**
     * Takes a long and creates the simple string format.
     * @param dateLong date stored as a long
     * @return formatted date in a string
     */
    public static String format(Long dateLong) {
        dateLong = dateLong*1000;
        Date date = new Date(dateLong);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
