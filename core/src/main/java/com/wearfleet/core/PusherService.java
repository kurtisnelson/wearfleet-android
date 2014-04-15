package com.wearfleet.core;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.ChatEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.events.PushEvent;

import java.util.Set;

import de.greenrobot.event.EventBus;

public class PusherService extends Service {
    private static final Gson gson = new Gson();
    private static final String TAG = "PusherService";
    private static final String AUTHORIZER_ENDPOINT = "http://my.wearfleet.com/users/pusher_auth?user_email=kurtisnelson@gmail.com&user_token=6exy5enz-KoXUa_qt9Kn";
    private Pusher pusher;
    private PrivateChannel deviceChannel;
    private PresenceChannel fleetChannel;
    private String fleetChannelName, deviceChannelName;
    private boolean fleetChannelActive, deviceChannelActive = false;
    private int deviceId = 1;
    private int fleetId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        HttpAuthorizer authorizer = new HttpAuthorizer(AUTHORIZER_ENDPOINT+"&device_id="+deviceId);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        deviceChannelName = "private-device_"+deviceId;
        fleetChannelName = "presence-fleet_"+fleetId;
        pusher = new Pusher(getString(R.string.pusher_key), options);
        EventBus.getDefault().registerSticky(this);
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

        if(fleetChannel == null) {
            fleetChannel = pusher.subscribePresence(fleetChannelName,
                    new PresenceChannelEventListener() {
                        @Override
                        public void onSubscriptionSucceeded(String channelName) {
                            fleetChannelActive = true;
                        }

                        @Override
                        public void onUsersInformationReceived(String channelName, Set<User> users) {

                        }

                        @Override
                        public void userSubscribed(String channelName, User user) {
                            if(EventBus.getDefault().getStickyEvent(LocationEvent.class) != null) {
                                Location l = EventBus.getDefault().getStickyEvent(LocationEvent.class).getLocation();
                                pushLocation(l);
                            }
                        }

                        @Override
                        public void userUnsubscribed(String channelName, User user) {

                        }

                        @Override
                        public void onAuthenticationFailure(String message, Exception e) {
                            Log.e(TAG, "Auth Failure: " + message);
                        }

                        @Override
                        public void onEvent(String channelName, String eventName, String data) {
                        }
                    }
            );
        }

        if(deviceChannel == null) {
            deviceChannel = pusher.subscribePrivate(deviceChannelName,
                    new PrivateChannelEventListener() {
                        @Override
                        public void onAuthenticationFailure(String message, Exception e) {
                            Log.e(TAG, "Auth Failure: " + message);
                        }

                        @Override
                        public void onSubscriptionSucceeded(String channelName) {
                            deviceChannelActive = true;
                            LocationEvent e = EventBus.getDefault().getStickyEvent(LocationEvent.class);
                            if (e != null) {
                                Location l = e.getLocation();
                                pushLocation(l);
                            }
                        }

                        @Override
                        public void onEvent(String channelName, String eventName, String data) {
                        }
                    }
            );
        }

        setupFleet();
        return START_STICKY;
    }

    private void setupFleet() {
        FleetEventListener fleetListener = new FleetEventListener();
        fleetChannel.bind("client-all", fleetListener);
        fleetChannel.bind("all", fleetListener);
        deviceChannel.bind("client-msg", fleetListener);
        deviceChannel.bind("msg", fleetListener);
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

    public void onEventAsync(ChatEvent e) {
        Log.d(TAG, e.getMessage());
        if(e.getMode() == ChatEvent.Mode.BROADCAST_OUT){
            e.setDevice(deviceId);
            fleetChannel.trigger("client-all", gson.toJson(e));
        }
    }

    public void onEventAsync(BearingEvent e) {
        pushBearing(e.getBearing());
    }

    private void pushLocation(Location l){
        if(deviceChannel != null && deviceChannelActive) {
            deviceChannel.trigger("client-location", "{\"device\":\"" + deviceId + "\", \"latitude\":\"" + l.getLatitude() + "\", \"longitude\":\"" + l.getLongitude() + "\"}");
        }
    }

    private void pushBearing(float bearing){
        if(deviceChannel != null && deviceChannelActive) {
            deviceChannel.trigger("client-bearing", "{\"device\":\"" + deviceId + "\", \"bearing\":\"" + bearing + "\"}");
        }
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
