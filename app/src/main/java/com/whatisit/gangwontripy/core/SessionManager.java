package com.whatisit.gangwontripy.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.whatisit.gangwontripy.data.model.LoginRes;

public class SessionManager {
    private static final String PREFS = "session_prefs";
    private static final String K_USER_ID = "user_id";
    private static final String K_NICK   = "nickname";
    private static final String K_AVATAR = "profile_image_url";

    private static SessionManager instance;
    private final SharedPreferences sp;

    private SessionManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
        return instance;
    }

    public void saveLogin(LoginRes res) {
        if (res == null || res.userId == null) return;
        sp.edit()
                .putLong(K_USER_ID, res.userId)
                .putString(K_NICK, res.nickname)
                .putString(K_AVATAR, res.profileImageUrl)
                .apply();
    }

    public boolean isLoggedIn() {
        return sp.contains(K_USER_ID) && getUserId() != -1L;
    }

    public long getUserId() {
        return sp.getLong(K_USER_ID, -1L);
    }

    public String getNickname() {
        return sp.getString(K_NICK, null);
    }

    public String getProfileImageUrl() {
        return sp.getString(K_AVATAR, null);
    }

    public void logout() {
        sp.edit().clear().apply();
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
