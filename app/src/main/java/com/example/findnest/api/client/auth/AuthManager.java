package com.example.findnest.api.client.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AuthManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveTokens(String accessToken, String refreshToken) {
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        editor.clear();
        editor.apply();
    }
}