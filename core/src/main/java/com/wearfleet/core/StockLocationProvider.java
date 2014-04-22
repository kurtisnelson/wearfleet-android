package com.wearfleet.core;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.utils.Config;

import de.greenrobot.event.EventBus;

public class StockLocationProvider implements Provider {
    protected static final String PREF_LOCATION_PERIOD = "LOCATION_PERIOD";
    protected static final int LOCATION_PERIOD_DEFAULT = 15000;
    private static final String TAG = "StockLocationProvider";
    private final LocationManager mLocationManager;

    public StockLocationProvider(Context c) {
        Log.d(TAG, "Activating");
        mLocationManager = (LocationManager) c.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);

        String locationProvider = mLocationManager.getBestProvider(criteria, true);

        mLocationManager.requestLocationUpdates(locationProvider, c.getSharedPreferences(Config.PREFS_NAME, c.MODE_PRIVATE).getInt(PREF_LOCATION_PERIOD, LOCATION_PERIOD_DEFAULT), 2, mLocationListener);
        Location lastLocation = mLocationManager.getLastKnownLocation(locationProvider);
        if(lastLocation == null)
            Log.w(TAG, "Null location!");
        EventBus.getDefault().postSticky(new LocationEvent(lastLocation));
    }

    public void shutdown() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location l) {
            EventBus.getDefault().postSticky(new LocationEvent(l));
            if (l.hasBearing())
                EventBus.getDefault().postSticky(new BearingEvent(l.getBearing()));
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
}
