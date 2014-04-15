package com.wearfleet.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PositionService extends Service {


    private static final String TAG = "PositionService";
    private Provider locationProvider;
    private Provider bearingProvider;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            locationProvider = new GoogleLocationProvider(this);
        } else {
            locationProvider = new StockLocationProvider(this);
        }
        bearingProvider = new BearingProvider(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationProvider.shutdown();
        bearingProvider.shutdown();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
