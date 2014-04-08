package com.wearfleet.core.gson;
import com.google.gson.annotations.SerializedName;

public class ChatData {
    @SerializedName("name")
    public String name;

    @SerializedName("message")
    public String message;
}
