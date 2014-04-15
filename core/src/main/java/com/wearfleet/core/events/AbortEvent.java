package com.wearfleet.core.events;

public class AbortEvent {
    private final String message;

    public AbortEvent(Exception e) {
        this.message = e.getMessage();
    }

    public AbortEvent(String s) {
        this.message = s;
    }

    public String getMessage() {
        return message;
    }
}
