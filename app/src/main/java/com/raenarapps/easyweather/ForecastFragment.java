package com.raenarapps.easyweather;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.raenarapps.easyweather.adapter.ForecastCursorAdapter;
import com.raenarapps.easyweather.data.WeatherContract.LocationEntry;
import com.raenarapps.easyweather.data.WeatherContract.WeatherEntry;
import com.raenarapps.easyweather.sync.WeatherSyncAdapter;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ForecastCursorAdapter.ViewHolderCallback {

    public static final String TAG = ForecastFragment.class.getSimpleName();
    public static final String ACTIVATED_POSITION_KEY = "ACTIVATED_POSITION_KEY";
    private ForecastCursorAdapter forecastAdapter;
    private RecyclerView recyclerView;
    public static final int FORECAST_LOADER_ID = 1;
    private int activatedPosition = -1;

    public interface Callback {
        public void onItemSelected(Uri uri);
    }

    public static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_COORD_LAT,
            LocationEntry.COLUMN_COORD_LONG
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_LOCATION_LAT = 7;
    public static final int COL_LOCATION_LONG = 8;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.content_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        forecastAdapter = new ForecastCursorAdapter(getActivity(), null, this);
        recyclerView.setAdapter(forecastAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(ACTIVATED_POSITION_KEY)) {
            activatedPosition = savedInstanceState.getInt(ACTIVATED_POSITION_KEY);
        }
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        WeatherSyncAdapter.syncImmediately(getContext());
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String locationStr = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
//        Intent intent = new Intent(getActivity(), WeatherService.class);
//        intent.putExtra(WeatherService.LOCATION_KEY, locationStr);
//        getActivity().startService(intent);
//


    }

    public void setIsTwoPane(boolean isTwoPane) {
        forecastAdapter.setUseTodayLayout(!isTwoPane);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getContext(), weatherForLocationUri,
                FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        if (activatedPosition != -1) {
            recyclerView.smoothScrollToPosition(activatedPosition);
            forecastAdapter.setActivatedPosition(activatedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void updateLocation() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    @Override
    public void onViewHolderClick(Uri uri, int position) {
        try {
            ((Callback) getActivity()).onItemSelected(uri);
            activatedPosition = position;
        } catch (ClassCastException e) {
            Log.e("Class Cast Exception", getActivity().getClass().getSimpleName()
                    + " must implement ForecastFragment.Callback");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activatedPosition != -1) {
            outState.putInt(ACTIVATED_POSITION_KEY, activatedPosition);
        }
    }
}
