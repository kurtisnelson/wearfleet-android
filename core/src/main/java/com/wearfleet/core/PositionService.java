package com.wearfleet.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.wearfleet.core.events.LocationEvent;

import de.greenrobot.event.EventBus;

public class PositionService extends Service {
    private static final String PREF_LOCATION_PERIOD = "LOCATION_PERIOD";
    private static final int LOCATION_PERIOD_DEFAULT = 15000;

    private LocationManager manager;
    private LocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location l) {
                EventBus.getDefault().postSticky(new LocationEvent(l));
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        for (String provider : manager.getProviders(true))
            manager.requestLocationUpdates(provider, getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).getInt(PREF_LOCATION_PERIOD, LOCATION_PERIOD_DEFAULT), 2, locationListener);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.removeUpdates(locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
