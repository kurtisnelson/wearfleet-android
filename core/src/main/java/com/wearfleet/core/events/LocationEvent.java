package com.wearfleet.core.events;

import android.location.Location;

public class LocationEvent {
    private final Location location;

    public LocationEvent(Location l) {
        this.location = l;
    }

    public Location getLocation() {
        return location;
    }
}
