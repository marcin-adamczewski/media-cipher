package com.appunite.mediaenryption.sample;


import android.app.Application;

import com.appunite.mediacipher.Config;
import com.appunite.mediacipher.MediaCipher;
import com.appunite.mediacipher.Listener;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final MediaCipher mediaCipher = MediaCipher.init(this, new Config(Config.LogLevel.DEBUG), new Listener() {
            @Override
            public void onKeystoreError(final Throwable throwable) {

            }

            @Override
            public void onError(final Throwable throwable) {

            }
        });
    }
}
