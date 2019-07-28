package com.gil.weatherapp.Common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    public static final String APP_ID = "3f1d788452832bc52485b9032d2c8ef0";
    public static Location current_location = null;

    public static String convertTextToDate(long dt) {
        Date date = new Date(dt*1000l);
        SimpleDateFormat sdf = new SimpleDateFormat("HH mm dd EEE MM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToHour(long  dt) {
        Date date = new Date(dt*1000l);
        SimpleDateFormat sdf = new SimpleDateFormat("HH mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
