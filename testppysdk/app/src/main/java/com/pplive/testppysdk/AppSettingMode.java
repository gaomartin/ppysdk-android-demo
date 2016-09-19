package com.pplive.testppysdk;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AppSettingMode
{
    public static String getSetting(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setSetting(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean getSetting(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void setSetting(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static int getIntSetting(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void setIntSetting(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static long getLongSetting(Context context, String key, long defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static void setLongSetting(Context context, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}