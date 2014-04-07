package com.wearfleet.core;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.events.PushEvent;

import java.util.Set;

import de.greenrobot.event.EventBus;

public class PusherService extends Service {
    private static final String TAG = "PusherService";
    private static final String AUTHORIZER_ENDPOINT = "http://my.wearfleet.com/users/pusher_auth?user_email=kurtisnelson@gmail.com&user_token=6exy5enz-KoXUa_qt9Kn";
    private Pusher pusher;
    private PresenceChannel fleetChannel;
    private String fleetChannelName = "presence-fleet";
    private boolean fleetChannelActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        HttpAuthorizer authorizer = new HttpAuthorizer(AUTHORIZER_ENDPOINT);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        pusher = new Pusher(getString(R.string.pusher_key), options);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.d(TAG, "State changed to " + change.getCurrentState() + " from " + change.getPreviousState());
                EventBus.getDefault().postSticky(change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.e(TAG, "Connection error: " + message, e);
            }
        }, ConnectionState.ALL);

        fleetChannel = pusher.subscribePresence(fleetChannelName,
                new PresenceChannelEventListener() {
                    @Override
                    public void onSubscriptionSucceeded(String channelName) {
                        fleetChannelActive = true;
                        Location l = EventBus.getDefault().getStickyEvent(LocationEvent.class).getLocation();
                        pushLocation(l);
                    }

                    @Override
                    public void onUsersInformationReceived(String channelName, Set<User> users) {

                    }

                    @Override
                    public void userSubscribed(String channelName, User user) {

                    }

                    @Override
                    public void userUnsubscribed(String channelName, User user) {

                    }

                    @Override
                    public void onAuthenticationFailure(String message, Exception e) {
                        Log.e(TAG, "Auth Failure: "+message);
                    }

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        EventBus.getDefault().post(new PushEvent(channelName, eventName, data));
                    }
        });
        EventBus.getDefault().registerSticky(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Shutting down");
        pusher.disconnect();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onEventAsync(LocationEvent e) {
        Location l = e.getLocation();
        pushLocation(l);
    }

    public void onEventAsync(BearingEvent e) {
        pushBearing(e.getBearing());
    }

    private void pushLocation(Location l){
        if(fleetChannel != null && fleetChannelActive)
            fleetChannel.trigger("client-location", "{\"latitude\":\""+l.getLatitude() +"\", \"longitude\":\""+l.getLongitude()+"\"}");
    }

    private void pushBearing(int bearing){
        if(fleetChannel != null && fleetChannelActive)
            fleetChannel.trigger("client-bearing", "{\"bearing\":\""+bearing+"\"}");
    }

    public static void start(Context c) {
        Intent i = new Intent(c, PusherService.class);
        c.startService(i);
        Intent i2 = new Intent(c, PositionService.class);
        c.startService(i2);
    }

    public static void stop(Context c) {
        Intent i = new Intent(c, PusherService.class);
        c.stopService(i);
        Intent i2 = new Intent(c, PositionService.class);
        c.stopService(i2);
    }

    public static boolean isRunning(Context c) {
            ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (PusherService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
    }
}
