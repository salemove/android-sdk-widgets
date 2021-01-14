package com.glia.widgets.helper;

import android.util.Log;

import com.glia.widgets.BuildConfig;

public class Logger {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }
}
