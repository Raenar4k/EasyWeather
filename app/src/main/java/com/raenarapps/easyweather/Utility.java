package com.raenarapps.easyweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis) {
        int diffInDays = getDiffInDays(dateInMillis);
        if (diffInDays == 0) {
            String today = context.getString(R.string.today);
            String format = context.getString(R.string.format_forecast);
            return String.format(format, today, getFormattedMonthDay(dateInMillis));
        } else if (diffInDays > 0 && diffInDays < 7) {
            return getDayName(context, dateInMillis);
        } else {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEEE d MMMM");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    private static int getDiffInDays(long dateInMillis) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        long currentDate = normalizeDate(gc.getTimeInMillis());
        long diffInMillis = dateInMillis - currentDate;
        return (int) (diffInMillis / (1000 * 60 * 60 * 24));
    }

    public static String getDayName(Context context, long dateInMillis) {
        int differenceInDays = getDiffInDays(dateInMillis);
        if (differenceInDays == 0) {
            return context.getString(R.string.today);
        } else if (differenceInDays == 1) {
            return context.getString(R.string.tomorrow);
        } else {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(long dateInMillis) {
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

    public static String getFormattedWind(Context context, double windSpeed, double degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        String direction = "";
        if (degrees >= 337.5 || degrees <= 22.5) {
            direction = context.getString(R.string.wind_N);
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = context.getString(R.string.wind_NE);
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = context.getString(R.string.wind_E);
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = context.getString(R.string.wind_SE);
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = context.getString(R.string.wind_S);
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = context.getString(R.string.wind_SW);
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = context.getString(R.string.wind_W);
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = context.getString(R.string.wind_NW);
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static String getFormattedHumidity(Context context, double humidity) {
        if (humidity > 0) {
            return context.getString(R.string.format_humidity, humidity);
        } else {
            return context.getString(R.string.humidity_NA);
        }
    }

    public static int getIconResourceForConditionId(int conditionId) {
        if (conditionId >= 200 && conditionId <= 232) {
            return R.drawable.ic_storm;
        } else if (conditionId >= 300 && conditionId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (conditionId >= 500 && conditionId <= 504) {
            return R.drawable.ic_rain;
        } else if (conditionId == 511) {
            return R.drawable.ic_snow;
        } else if (conditionId >= 520 && conditionId <= 531) {
            return R.drawable.ic_rain;
        } else if (conditionId >= 600 && conditionId <= 622) {
            return R.drawable.ic_snow;
        } else if (conditionId >= 701 && conditionId <= 761) {
            return R.drawable.ic_fog;
        } else if (conditionId == 761 || conditionId == 781) {
            return R.drawable.ic_storm;
        } else if (conditionId == 800) {
            return R.drawable.ic_clear;
        } else if (conditionId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (conditionId >= 802 && conditionId <= 804) {
            return R.drawable.ic_clouds;
        }
        return -1;
    }

    public static int getArtResourceForConditionId(int conditionId) {
        if (conditionId >= 200 && conditionId <= 232) {
            return R.drawable.art_storm;
        } else if (conditionId >= 300 && conditionId <= 321) {
            return R.drawable.art_light_rain;
        } else if (conditionId >= 500 && conditionId <= 504) {
            return R.drawable.art_rain;
        } else if (conditionId == 511) {
            return R.drawable.art_snow;
        } else if (conditionId >= 520 && conditionId <= 531) {
            return R.drawable.art_rain;
        } else if (conditionId >= 600 && conditionId <= 622) {
            return R.drawable.art_snow;
        } else if (conditionId >= 701 && conditionId <= 761) {
            return R.drawable.art_fog;
        } else if (conditionId == 761 || conditionId == 781) {
            return R.drawable.art_storm;
        } else if (conditionId == 800) {
            return R.drawable.art_clear;
        } else if (conditionId == 801) {
            return R.drawable.art_light_clouds;
        } else if (conditionId >= 802 && conditionId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }
}
