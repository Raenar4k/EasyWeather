package com.raenarapps.easyweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

//http://developer.android.com/training/sync-adapters/creating-sync-adapter.html#CreateSyncAdapterService
public class WeatherSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static WeatherSyncAdapter sWeatherSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("WeatherSyncService", "onCreate - WeatherSyncService");
        /* instantiate the component in a thread-safe manner,
        in case the sync adapter framework queues up multiple executions of your sync adapter
         in response to triggers or scheduling. */
        synchronized (sSyncAdapterLock) {
            if (sWeatherSyncAdapter == null) {
                sWeatherSyncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sWeatherSyncAdapter.getSyncAdapterBinder();
    }
}