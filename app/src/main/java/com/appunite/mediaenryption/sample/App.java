package com.appunite.mediaenryption.sample;


import android.app.Application;

import com.appunite.mediaenryption.Config;
import com.appunite.mediaenryption.MediaEncrypter;
import com.appunite.mediaenryption.crypto.Listener;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final MediaEncrypter mediaEncrypter = MediaEncrypter.init(this, new Config(Config.LogLevel.DEBUG), new Listener() {
            @Override
            public void onKeystoreError(final Throwable throwable) {

            }

            @Override
            public void onError(final Throwable throwable) {

            }
        });
    }
}
