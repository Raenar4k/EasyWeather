package com.raenarapps.easyweather;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.raenarapps.easyweather.data.WeatherContract;
import com.raenarapps.easyweather.data.WeatherContract.LocationEntry;
import com.raenarapps.easyweather.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Vector;

public class WeatherService extends IntentService {
    private static final String SERVICE_NAME = WeatherService.class.getSimpleName();
    private static final String LOG_TAG = WeatherService.class.getSimpleName();
    private static final String MY_API_KEY = "7696b400d1eee64d5870fdb450179396";

    public static final String LOCATION_KEY = "LOCATION_KEY";
    public static final int NOTIFICATION_ID = 111;

    public WeatherService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery;
        if (intent != null && intent.hasExtra(LOCATION_KEY)) {
            locationQuery = intent.getStringExtra(LOCATION_KEY);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 14;

            try {

                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                final String LANGUAGE_PARAM = "lang";

                String lang;
                String currentLanguage = Locale.getDefault().getLanguage();
                if (currentLanguage.equals("ru")) {
                    lang = "ru";
                } else {
                    lang = "en";
                }
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, locationQuery)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, MY_API_KEY)
                        .appendQueryParameter(LANGUAGE_PARAM, lang)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() != 0) {
                        forecastJsonStr = buffer.toString();
                        getWeatherDataFromJson(forecastJsonStr, locationQuery);
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }
    }

    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSetting)
            throws JSONException {

        // Location information in "city" object
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";

        // Coordinates are in "coord" object
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "description";
        final String OWM_WEATHER_ID = "id";
        final String OWM_DATE_TIME = "dt";


        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            for (int i = 0; i < weatherArray.length(); i++) {
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long unixTime = dayForecast.getLong(OWM_DATE_TIME);
                dateTime = unixTime * 1000;

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }
            int inserted = 0;
            if (cVVector.size() > 0) {
                ContentValues[] arrayCV = new ContentValues[cVVector.size()];
                cVVector.toArray(arrayCV);
                inserted = this.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, arrayCV);
                cleanUpOldData(locationId);
            }

            Log.d(LOG_TAG, "Done fetching weather. " + inserted + " Inserted");
            if (Utility.areNotificationsEnabled(this)) {
                showNotification();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void cleanUpOldData(long locationId) {
        int deleted;
        long currentDateInMillis = WeatherContract.normalizeDate(System.currentTimeMillis());
        String selection = WeatherEntry.COLUMN_DATE + " < ? AND " + WeatherEntry.COLUMN_LOC_KEY + " == ?";
        String[] selectionArgs = {Long.toString(currentDateInMillis), Long.toString(locationId)};
        deleted = this.getContentResolver().delete(WeatherEntry.CONTENT_URI, selection, selectionArgs );
        Log.d(LOG_TAG, "Cleaned up. " + deleted + " Deleted");
    }

    private void showNotification() {
        String todayForecast = getTodayForecastString(this);
        int weatherID = getTodayForecastWeatherID(this);
        Intent intent = new Intent(this,MainActivity.class);
        TaskStackBuilder.create(this).addNextIntent(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(Utility.getIconResourceForConditionId(weatherID))
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText(todayForecast)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private int getTodayForecastWeatherID(Context context) {
        String[] projection = {
                WeatherEntry.TABLE_NAME + WeatherEntry._ID,
                WeatherEntry.COLUMN_WEATHER_ID

        };
        Uri uri = WeatherEntry.buildWeatherLocationWithDate(
                Utility.getPreferredLocation(this),
                System.currentTimeMillis());
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            int weatherID = c.getInt(1);
            return weatherID;
        }
        return 0;
    }

    private String getTodayForecastString(Context context) {
        String[] projection = {
                WeatherEntry.TABLE_NAME + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_SHORT_DESC,

        };
        Uri uri = WeatherEntry.buildWeatherLocationWithDate(
                Utility.getPreferredLocation(this),
                System.currentTimeMillis());
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            double high = c.getDouble(2);
            double low = c.getDouble(3);
            String description = c.getString(4);
            return getString(R.string.format_notification, description, high, low);
        }
        return null;
    }

    public long addLocation(String locationSetting, String cityName, double lat, double lon) {

        Cursor cursor = this.getContentResolver()
                .query(LocationEntry.CONTENT_URI, null, LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                        new String[]{locationSetting}, null);
        long id;
        if (cursor.getCount() != 0) {
            int idIndex = cursor.getColumnIndex(LocationEntry._ID);
            cursor.moveToFirst();
            id = cursor.getLong(idIndex);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            cv.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            cv.put(LocationEntry.COLUMN_COORD_LAT, lat);
            cv.put(LocationEntry.COLUMN_COORD_LONG, lon);
            Uri uri = this.getContentResolver().insert(LocationEntry.CONTENT_URI, cv);
            id = ContentUris.parseId(uri);
        }
        cursor.close();
        return id;
    }


}
