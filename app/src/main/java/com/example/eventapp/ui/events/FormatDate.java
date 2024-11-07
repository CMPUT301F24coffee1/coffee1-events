package com.example.eventapp.ui.events;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatDate {

    public static String format(Long dateLong) {
        dateLong = dateLong*1000;
        Date date = new Date(dateLong);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
