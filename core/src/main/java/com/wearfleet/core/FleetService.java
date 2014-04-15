package com.wearfleet.core;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public abstract class FleetService extends Service  {
    public static final String STOP_SERVICE_FILTER = "STOP_FLEET_SERVICE";
    private PusherManager pusherManager;

    @Override
    public void onCreate() {
        super.onCreate();
        pusherManager = new PusherManager(getString(R.string.pusher_key));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pusherManager.start();
        Intent i2 = new Intent(this, PositionService.class);
        this.startService(i2);
        registerReceiver(stopServiceReceiver, new IntentFilter(STOP_SERVICE_FILTER));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pusherManager.shutdown();
        Intent i2 = new Intent(this, PositionService.class);
        this.stopService(i2);
        unregisterReceiver(stopServiceReceiver);
    }

    public static boolean isRunning(Context c, Class childClass) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (childClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
}
