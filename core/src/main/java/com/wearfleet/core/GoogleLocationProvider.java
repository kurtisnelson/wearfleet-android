package com.wearfleet.core;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.utils.Config;

import de.greenrobot.event.EventBus;

public class GoogleLocationProvider implements
        Provider,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    protected static final String PREF_LOCATION_PERIOD = "LOCATION_PERIOD";
    protected static final int LOCATION_PERIOD_DEFAULT = 15000;
    private static final int FASTEST_INTERVAL = 1000;
    private static final String TAG = "GoogleLocationProvider";

    private final LocationRequest mLocationRequest;
    private final LocationClient mLocationClient;

    public GoogleLocationProvider(Context c) {
        Log.d(TAG, "Activating");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(c.getSharedPreferences(Config.PREFS_NAME, c.MODE_PRIVATE).getInt(PREF_LOCATION_PERIOD, LOCATION_PERIOD_DEFAULT));
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationClient = new LocationClient(c, this, this);
        mLocationClient.connect();
        EventBus.getDefault().postSticky(new LocationEvent(mLocationClient.getLastLocation()));
    }

    @Override
    public void shutdown() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(mLocationListener);
        }
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationListener);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location l) {
            EventBus.getDefault().postSticky(new LocationEvent(l));
            if (l.hasBearing())
                EventBus.getDefault().postSticky(new BearingEvent(l.getBearing()));
        }
    };
}
