package com.wearfleet.core;

import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PresenceChannel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.wearfleet.core.events.AbortEvent;
import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.ChatEvent;
import com.wearfleet.core.events.LocationEvent;

import java.util.Set;

import de.greenrobot.event.EventBus;

public class PusherManager {
    private static final Gson gson = new Gson();
    private static final String TAG = "PusherManager";
    private static final String AUTHORIZER_ENDPOINT = "http://my.wearfleet.com/users/pusher_auth?user_email=kurtisnelson@gmail.com&user_token=6exy5enz-KoXUa_qt9Kn";
    private Pusher pusher;
    private PrivateChannel deviceChannel;
    private PresenceChannel fleetChannel;
    private String fleetChannelName, deviceChannelName;
    private boolean fleetChannelActive, deviceChannelActive = false;
    private int deviceId = 1;
    private int fleetId = 1;

    public PusherManager(String pusherKey) {
        HttpAuthorizer authorizer = new HttpAuthorizer(AUTHORIZER_ENDPOINT + "&device_id=" + deviceId);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        deviceChannelName = "private-device_" + deviceId;
        fleetChannelName = "presence-fleet_" + fleetId;
        pusher = new Pusher(pusherKey, options);
        EventBus.getDefault().registerSticky(this);
    }

    public void start() {
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.d(TAG, "State changed to " + change.getCurrentState() + " from " + change.getPreviousState());
                EventBus.getDefault().postSticky(change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                EventBus.getDefault().post(new AbortEvent(e));
            }
        }, ConnectionState.ALL);

        if (fleetChannel == null) {
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
                            if (EventBus.getDefault().getStickyEvent(LocationEvent.class).getLocation() != null) {
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

        if (deviceChannel == null) {
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
                                if(l != null)
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
    }



    private void setupFleet() {
        FleetEventListener fleetListener = new FleetEventListener();
        fleetChannel.bind("client-all", fleetListener);
        fleetChannel.bind("all", fleetListener);
        deviceChannel.bind("client-msg", fleetListener);
        deviceChannel.bind("msg", fleetListener);
    }

    public void shutdown() {
        Log.d(TAG, "Shutting down");
        pusher.disconnect();
        EventBus.getDefault().unregister(this);
    }

    public void onEventAsync(LocationEvent e) {
        Location l = e.getLocation();
        pushLocation(l);
    }

    public void onEventAsync(ChatEvent e) {
        Log.d(TAG, e.getMessage());
        if (e.getMode() == ChatEvent.Mode.BROADCAST_OUT) {
            e.setDevice(deviceId);
            fleetChannel.trigger("client-all", gson.toJson(e));
        }
    }

    public void onEventAsync(BearingEvent e) {
        pushBearing(e.getBearing());
    }

    private void pushLocation(Location l) {
        if (deviceChannel != null && deviceChannelActive) {
            deviceChannel.trigger("client-location", "{\"device\":\"" + deviceId + "\", \"latitude\":\"" + l.getLatitude() + "\", \"longitude\":\"" + l.getLongitude() + "\"}");
        }
    }

    private void pushBearing(float bearing) {
        if (deviceChannel != null && deviceChannelActive) {
            deviceChannel.trigger("client-bearing", "{\"device\":\"" + deviceId + "\", \"bearing\":\"" + bearing + "\"}");
        }
    }
}
