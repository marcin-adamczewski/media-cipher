package com.appunite.mediacipher.helpers;


import android.util.Log;

import com.appunite.mediacipher.MediaCipher;

public class Logger {

    private static final String TAG = "MediaEncryption";

    public static void logDebug(final String message) {
        if (isDebugLoggingEnabled()) {
            Log.d(TAG, message);
        }
    }

    public static void logDebug(final String tag, final String message) {
        if (isDebugLoggingEnabled()) {
            Log.d(tag, message);
        }
    }

    public static void logError(final String message) {
        if (isErrorLoggingEnabled()) {
            Log.e(TAG, message);
        }
    }

    public static void logError(final String tag, final String message) {
        if (isErrorLoggingEnabled()) {
            Log.e(tag, message);
        }
    }

    private static boolean isErrorLoggingEnabled() {
        return MediaCipher.getInstance().getConfig().isErrorLoggingEnabled();
    }

    private static boolean isDebugLoggingEnabled() {
        return MediaCipher.getInstance().getConfig().isDebugLoggingEnabled();
    }
}
