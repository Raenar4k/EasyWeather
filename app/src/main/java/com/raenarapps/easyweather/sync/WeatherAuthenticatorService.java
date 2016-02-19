package com.raenarapps.easyweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

//allows the sync adapter framework to access the authenticator.

public class WeatherAuthenticatorService extends Service {
    private WeatherAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new WeatherAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
