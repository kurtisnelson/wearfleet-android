package com.wearfleet.core;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.widget.Toast;

import com.wearfleet.core.events.AbortEvent;
import com.wearfleet.core.utils.Config;

import de.greenrobot.event.EventBus;

public abstract class FleetService extends Service  {
    public static final String STOP_SERVICE_FILTER = "STOP_FLEET_SERVICE";
    private PusherManager pusherManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Config config = new Config(this);
        if(!config.isAuthenticated()) {
            Toast.makeText(this, getString(R.string.not_authenticated), Toast.LENGTH_LONG);
            stopSelf();
        }else {
            pusherManager = new PusherManager(getString(R.string.pusher_key), config);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(stopServiceReceiver, new IntentFilter(STOP_SERVICE_FILTER));
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            pusherManager.start();
            Intent i2 = new Intent(this, PositionService.class);
            this.startService(i2);
        } else {
            EventBus.getDefault().post(new AbortEvent("No Network"));
            stopSelf();
        }
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

    public void onEvent(AbortEvent e) {
        stopSelf();
    }
}
