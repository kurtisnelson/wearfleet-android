package com.wearfleet.core.events;

public class PushEvent {
    private final String eventName;
    private final String channelName;
    private final String data;

    public PushEvent(String channelName, String eventName, String data) {
        this.channelName = channelName;
        this.eventName = eventName;
        this.data = data;
    }
}
