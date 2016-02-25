package com.raenarapps.easyweather;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.raenarapps.easyweather.pojo.City;
import com.raenarapps.easyweather.pojo.Coord;
import com.raenarapps.easyweather.pojo.Forecast;
import com.raenarapps.easyweather.pojo.OWMModel;
import com.raenarapps.easyweather.pojo.Temp;
import com.raenarapps.easyweather.pojo.Weather;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public class WeatherService extends IntentService {
    private static final String SERVICE_NAME = WeatherService.class.getSimpleName();
    private static final String LOG_TAG = WeatherService.class.getSimpleName();
    private static final String MY_API_KEY = "7696b400d1eee64d5870fdb450179396";

    public static final String LOCATION_KEY = "LOCATION_KEY";
    public static final int NOTIFICATION_ID = 111;

    public WeatherService() {
        super(SERVICE_NAME);
    }

    public interface OWMService {
        @GET("daily")
        Call<OWMModel> getOWMDailyResponse(@QueryMap Map<String, String> options);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery;
        if (intent != null && intent.hasExtra(LOCATION_KEY)) {
            locationQuery = intent.getStringExtra(LOCATION_KEY);

            String format = "json";
            String units = "metric";
            int numDays = 14;

            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/";
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
            HashMap<String, String> options = new HashMap<String, String>();
            options.put(QUERY_PARAM, locationQuery);
            options.put(FORMAT_PARAM, format);
            options.put(UNITS_PARAM, units);
            options.put(DAYS_PARAM, Integer.toString(numDays));
            options.put(APPID_PARAM, MY_API_KEY);
            options.put(LANGUAGE_PARAM, lang);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(FORECAST_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            OWMService service = retrofit.create(OWMService.class);
            Call<OWMModel> call = service.getOWMDailyResponse(options);
            try {
                Response<OWMModel> response = call.execute();
                OWMModel model = response.body();
                getWeatherDataFromModel(model, locationQuery);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getWeatherDataFromModel(OWMModel model,
                                         String locationSetting) {

        List<Forecast> forecastList = model.getList();

        City city = model.getCity();
        String cityName = city.getName();

        Coord coord = city.getCoord();
        double cityLatitude = coord.getLat();
        double cityLongitude = coord.getLon();

        long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(forecastList.size());

        for (int i = 0; i < forecastList.size(); i++) {
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            Forecast dayForecast = forecastList.get(i);

            long unixTime = dayForecast.getDt();
            dateTime = unixTime * 1000;

            pressure = dayForecast.getPressure();
            humidity = dayForecast.getHumidity();
            windSpeed = dayForecast.getSpeed();
            windDirection = dayForecast.getDeg();

            Weather weatherObject = dayForecast.getWeather().get(0);
            description = weatherObject.getDescription();
            weatherId = weatherObject.getId();

            Temp temperatureObject = dayForecast.getTemp();
            high = temperatureObject.getMax();
            low = temperatureObject.getMin();

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
    }

    private void cleanUpOldData(long locationId) {
        int deleted;
        long currentDateInMillis = WeatherContract.normalizeDate(System.currentTimeMillis());
        String selection = WeatherEntry.COLUMN_DATE + " < ? AND " + WeatherEntry.COLUMN_LOC_KEY + " == ?";
        String[] selectionArgs = {Long.toString(currentDateInMillis), Long.toString(locationId)};
        deleted = this.getContentResolver().delete(WeatherEntry.CONTENT_URI, selection, selectionArgs);
        Log.d(LOG_TAG, "Cleaned up. " + deleted + " Deleted");
    }

    private void showNotification() {
        String todayForecast = getTodayForecastString(this);
        int weatherID = getTodayForecastWeatherID(this);
        Intent intent = new Intent(this, MainActivity.class);
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

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String locationStr = Utility.getPreferredLocation(context);
            Intent serviceIntent = new Intent(context, WeatherService.class);
            serviceIntent.putExtra(WeatherService.LOCATION_KEY, locationStr);
            context.startService(serviceIntent);
        }
    }

    public static class BootReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Utility.setUpRepeatingAlarm(context);
            }
        }
    }


}
