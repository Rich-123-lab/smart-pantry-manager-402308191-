package com.Welsh.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User settings, saved in SharedPreferences (a small key/value file on the
 * device), so they are remembered after the app is closed.
 */
public final class AppPrefs {

    private static final String FILE = "smart_pantry_settings";
    public static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    public static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    public static final String KEY_IGNORE_EXPIRED = "ignore_expired";

    private AppPrefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean highlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, true);
    }

    public static boolean showAlmostThere(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public static boolean ignoreExpired(Context context) {
        return prefs(context).getBoolean(KEY_IGNORE_EXPIRED, true);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        prefs(context).edit().putBoolean(key, value).apply();
    }
}
