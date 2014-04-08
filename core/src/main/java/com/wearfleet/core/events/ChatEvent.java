package com.wearfleet.core.events;

import com.google.gson.Gson;
import com.wearfleet.core.gson.ChatData;

public class ChatEvent {
    private final String name;
    private final String message;
    private final Mode mode;

    public enum Mode {
        DEVICE, BROADCAST
    }

    private static Gson gson = new Gson();

    public ChatEvent(String name, String message, Mode mode) {
        this.name = name;
        this.message = message;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public Mode getMode() {
        return mode;
    }

    public String getMessage() {
        return message;
    }

    public static ChatEvent newBroadcast(String json) {
        ChatData msg = parseJson(json);
        return new ChatEvent(msg.name, msg.message, Mode.BROADCAST);
    }

    private static ChatData parseJson(String json) {
        return gson.fromJson(json, ChatData.class);
    }

    public static ChatEvent newDevice(String json) {
        ChatData msg = parseJson(json);
        return new ChatEvent(msg.name, msg.message, Mode.DEVICE);
    }
}


