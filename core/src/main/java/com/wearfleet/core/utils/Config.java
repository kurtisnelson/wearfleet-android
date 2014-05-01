package com.wearfleet.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    public static final String PREFS_NAME = "WEARFLEET_PREF";
    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String USER_TOKEN = "USER_TOKEN";
    private static final String API_ENDPOINT = "API_ENDPOINT";
    private Context context;

    public Config(Context c){
        this.context = c;
    }

    public boolean isAuthenticated(){
        if(getApiEndpoint() == null || getUserEmail() == null || getUserToken() == null)
            return false;
        return true;
    }

    public String getPusherAuthUrl(){
        return getApiEndpoint() + "/users/pusher_auth?user_email=" + getUserEmail() + "&user_token=" + getUserToken() +"&device_id=" + getDeviceId();
    }

    public int getDeviceId() {
        return 1;
    }

    public String getApiEndpoint() {
        return sharedPreferences(context).getString(API_ENDPOINT, null);
    }

    public String getUserEmail() {
        return sharedPreferences(context).getString(USER_EMAIL, null);
    }

    public String getUserToken() {
        return sharedPreferences(context).getString(USER_TOKEN, null);
    }

    public int getFleetId() {
        return 1;
    }

    private static SharedPreferences sharedPreferences(Context c) {
        return c.getSharedPreferences(Config.PREFS_NAME, c.MODE_PRIVATE);
    }

    public static void setFromString(Context c, String string) {
        String[] values = string.split(";");
        SharedPreferences.Editor editor = sharedPreferences(c).edit();
        editor.putString(USER_EMAIL, values[0]);
        editor.putString(USER_TOKEN, values[1]);
        editor.putString(API_ENDPOINT, values[2]);
        editor.apply();
    }

    public void clearAuthentication() {
        SharedPreferences.Editor editor = sharedPreferences(context).edit();
        editor.putString(USER_EMAIL, null);
        editor.putString(USER_TOKEN, null);
        editor.putString(API_ENDPOINT, null);
        editor.apply();
    }
}
