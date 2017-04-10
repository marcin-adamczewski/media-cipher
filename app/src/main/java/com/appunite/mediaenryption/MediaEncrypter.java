package com.appunite.mediaenryption;


import android.content.Context;

import com.appunite.mediaenryption.crypto.AESCrypter;
import com.appunite.mediaenryption.crypto.AESCrypterBelowM;
import com.appunite.mediaenryption.crypto.AESCrypterMPlus;

import javax.annotation.Nonnull;

public final class MediaEncrypter {

    private static volatile MediaEncrypter singleton;
    private final Context context;
    private Config config;
    private AESCrypter aesCrypter;

    public static void init(@Nonnull final Context context) {
        init(context, new Config());
    }

    public static MediaEncrypter init(@Nonnull final Context context, @Nonnull final Config config) {
        if (singleton == null) {
            synchronized(MediaEncrypter.class) {
                if (singleton == null) {
                    singleton = new MediaEncrypter(context, config);
                }
            }
        }

        return singleton;
    }

    public static MediaEncrypter getInstance() {
        return singleton;
    }

    public static class Config {
        private boolean loggingEnabled;

        public Config setLoggingEnabled() {
            loggingEnabled = true;
            return this;
        }

        public boolean isLoggingEnabled() {
            return loggingEnabled;
        }

    }

    public Config getConfig() {
        return config;
    }

    private MediaEncrypter(final Context context, final Config config) {
        this.context = context;
        this.config = config;
        final KeysPreferences keysPreferences = new KeysPreferences(context);
        this.aesCrypter = VersionsUtils.isAtLeastMarshMallow() ?
                new AESCrypterMPlus(context, keysPreferences) : new AESCrypterBelowM(context, keysPreferences);
    }
}
