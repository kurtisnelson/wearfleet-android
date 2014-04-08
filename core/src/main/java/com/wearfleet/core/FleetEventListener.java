package com.wearfleet.core;

import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.User;
import com.wearfleet.core.events.ChatEvent;
import com.wearfleet.core.events.PushEvent;

import java.util.Set;

import de.greenrobot.event.EventBus;

public class FleetEventListener implements PresenceChannelEventListener {

    private static final String TAG = "FleetEventListener";

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        Object event;
        if(eventName.equals("client-all") || eventName.equals("all")){
            event = ChatEvent.newBroadcast(data);
        }else if(eventName.equals("client-msg") || eventName.equals("msg")){
            event = ChatEvent.newDevice(data);
        }else{
           event = new PushEvent(channelName, eventName, data);
        }
        EventBus.getDefault().post(event);
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

    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {

    }
}
