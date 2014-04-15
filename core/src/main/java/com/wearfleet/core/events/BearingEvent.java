package com.wearfleet.core.events;

public class BearingEvent {
    private final float bearing;

    public BearingEvent(float bearing) {
        this.bearing = bearing;
    }

    public float getBearing() {
        return bearing;
    }
}
