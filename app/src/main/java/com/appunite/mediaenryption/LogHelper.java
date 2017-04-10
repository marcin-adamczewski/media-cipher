package com.appunite.mediaenryption;


import android.util.Log;

public class LogHelper {

    private static final String TAG = "MediaEncryption";

    public LogHelper() {
    }

    public static void logIfDebug(final String message) {
        if (MediaEncrypter.getInstance().getConfig().isLoggingEnabled()) {
            Log.d(TAG, message);
        }
    }

    public static void logIfDebug(final String tag, final String message) {
        if (MediaEncrypter.getInstance().getConfig().isLoggingEnabled()) {
            Log.d(tag, message);
        }
    }
}
