package com.appunite.mediaenryption.sample;


import android.app.Application;
import android.os.StrictMode;

import com.appunite.mediacipher.Config;
import com.appunite.mediacipher.MediaCipher;
import com.appunite.mediacipher.Listener;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        MediaCipher.init(this, new Config(Config.LogLevel.DEBUG), new Listener() {
            @Override
            public void onKeyCorrupted(final Throwable throwable) {
                // When key is corrupted you cannot use it anymore and probably you want to remove
                // all already encrypted files. More here https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.on3ZPwjc.dpbs
            }

            @Override
            public void onError(final Throwable throwable) {

            }
        });
    }
}
