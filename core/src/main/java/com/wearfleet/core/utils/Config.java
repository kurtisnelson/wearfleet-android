package com.wearfleet.core.utils;

public class Config {
    public static final String PREFS_NAME = "WEARFLEET_PREF";

    public static String getPusherAuthUrl(){
        return getApiEndpoint() + "/users/pusher_auth?user_email=" + getUserEmail() + "&user_token=" + getUserToken() +"&device_id=" + getDeviceId();
    }

    public static int getDeviceId() {
        return 1;
    }

    public static String getApiEndpoint() {
        return "https://mywearfleet.herokuapp.com";
    }

    public static String getUserEmail() {
        return "kurtisnelson@gmail.com";
    }

    public static String getUserToken() {
        return "trash";
    }

    public static int getFleetId() {
        return 1;
    }
}
