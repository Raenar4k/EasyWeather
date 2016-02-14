package com.raenarapps.easyweather;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.raenarapps.easyweather.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;
    public static final String DETAIL_URI = "DETAIL_URI";
    private ShareActionProvider shareActionProvider;
    private String forecastString;
    private Uri uri;

    public static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_DEGREES = 7;
    private static final int COL_WEATHER_PRESSURE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private TextView dayView;
    private TextView dateView;
    private ImageView iconView;
    private TextView descriptionView;
    private TextView tempHighView;
    private TextView tempLowView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            uri = bundle.getParcelable(DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.content_detail, container, false);
        dayView = (TextView) rootView.findViewById(R.id.detail_day);
        dateView = (TextView) rootView.findViewById(R.id.detail_date);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        descriptionView = ((TextView) rootView.findViewById(R.id.detail_description));
        tempHighView = ((TextView) rootView.findViewById(R.id.detail_temp_high));
        tempLowView = ((TextView) rootView.findViewById(R.id.detail_temp_low));
        humidityView = ((TextView) rootView.findViewById(R.id.detail_humidity));
        windView = ((TextView) rootView.findViewById(R.id.detail_wind));
        pressureView = ((TextView) rootView.findViewById(R.id.detail_pressure));
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (shareActionProvider != null && forecastString != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(TAG, "ShareActionProvider = null");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (uri == null) {
            return null;
        }
        return new CursorLoader(getContext(), uri,
                FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        Context context = getContext();
        String description = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getContext());
        String tempHigh = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String tempLow = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
        String humidityString = Utility.getFormattedHumidity(context, humidity);
        double windSpeed = data.getDouble(COL_WEATHER_WIND_SPEED);
        double degrees = data.getDouble(COL_WEATHER_DEGREES);
        String windString = Utility.getFormattedWind(context, windSpeed, degrees);
        String pressureString = context.getString(R.string.format_pressure, data.getDouble(COL_WEATHER_PRESSURE));
        int conditionId = data.getInt(COL_WEATHER_CONDITION_ID);

        dayView.setText(Utility.getDayName(context, data.getLong(COL_WEATHER_DATE)));
        dateView.setText(Utility.getFormattedMonthDay(data.getLong(COL_WEATHER_DATE)));
        iconView.setImageResource(Utility.getArtResourceForConditionId(conditionId));
        descriptionView.setText(description);
        tempHighView.setText(tempHigh);
        tempLowView.setText(tempLow);
        humidityView.setText(humidityString);
        windView.setText(windString);
        pressureView.setText(pressureString);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateLocation(String locationSetting) {
        Uri oldUri = uri;
        if (oldUri != null) {
            long date = WeatherEntry.getDateFromUri(uri);
            Uri newUri = WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
            uri = newUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}
