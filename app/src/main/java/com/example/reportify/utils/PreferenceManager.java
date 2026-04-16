package com.example.reportify.utils;

import android.content.Context;
import android.content.SharedPreferences;



public class PreferenceManager {

    private static final String PREF_NAME = "smart_complaint_prefs";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private static final String KEY_RADIUS = "search_radius";


    private SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSelectedTab(int tabId) {
        preferences.edit().putInt(KEY_SELECTED_TAB, tabId).apply();
    }

    public int getSelectedTab() {
        return preferences.getInt(KEY_SELECTED_TAB, -1);
    }

    public void saveRadius(double radius) {
        preferences.edit().putFloat(KEY_RADIUS, (float) radius).apply();
    }

    public double getRadius() {
        return preferences.getFloat(KEY_RADIUS, 5.0f);
    }

}
