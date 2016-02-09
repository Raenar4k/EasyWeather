package com.raenarapps.easyweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utility {

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        long currentDate = normalizeDate(gc.getTimeInMillis());

        long diffInMillis = dateInMillis - currentDate;
        int diffInDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));

        if (diffInDays == 0) {
            String today = context.getString(R.string.today);
            String format = context.getString(R.string.format);
            return String.format(format, today, getFormattedMonthDay(dateInMillis));
        } else if (diffInDays > 0 && diffInDays < 7) {
            return getDayName(context, dateInMillis, diffInDays);
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEEE d MMMM");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    private static String getDayName(Context context, long dateInMillis, int differenceInDays) {
        if (differenceInDays == 1) {
            return context.getString(R.string.tomorrow);
        } else {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    private static String getFormattedMonthDay(long dateInMillis) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("d MMMM");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static long normalizeDate(long timeInMillis) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        gc.setTimeInMillis(timeInMillis);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTimeInMillis();
    }
}
