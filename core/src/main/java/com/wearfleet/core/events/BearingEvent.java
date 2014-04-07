package com.wearfleet.core.events;

public class BearingEvent {
    private final int bearing;

    public BearingEvent(int bearing){
        this.bearing = bearing;
    }

    public int getBearing() {
        return bearing;
    }
}
