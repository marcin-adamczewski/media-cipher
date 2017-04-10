package com.appunite.mediaenryption;


import android.os.Build;

public class VersionsUtils {

    public static boolean isAtLeastMarshMallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
