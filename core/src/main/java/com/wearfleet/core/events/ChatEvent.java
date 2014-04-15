package com.wearfleet.core.events;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ChatEvent {
    @SerializedName("name")
    private final String name;

    @SerializedName("message")
    private final String message;
    private Mode mode;

    @SerializedName("device_id")
    private int device;

    public ChatEvent(String spokenText) {
        this.message = spokenText;
        this.mode = Mode.BROADCAST_OUT;
        this.name = "Me";
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public enum Mode {
        DEVICE_IN, BROADCAST_IN, DEVICE_OUT, BROADCAST_OUT
    }

    private static Gson gson = new Gson();

    public ChatEvent(String name, String message, Mode mode) {
        this.name = name;
        this.message = message;
        this.mode = mode;
    }

    @Override
    public String toString() {
        return name + ": " + message;
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
        ChatEvent msg = parseJson(json);
        msg.mode = Mode.BROADCAST_IN;
        return msg;
    }

    private static ChatEvent parseJson(String json) {
        return gson.fromJson(json, ChatEvent.class);
    }

    public static ChatEvent newDevice(String json) {
        ChatEvent msg = parseJson(json);
        msg.mode = Mode.DEVICE_IN;
        return msg;
    }
}


