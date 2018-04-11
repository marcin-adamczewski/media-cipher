package com.appunite.mediacipher;


import android.app.Activity;
import android.content.Context;

import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.crypto.AESCrypterBelowM;
import com.appunite.mediacipher.crypto.AESCrypterMPlus;
import com.appunite.mediacipher.crypto.download.DownloadEncryptingOutputStream;
import com.appunite.mediacipher.crypto.exoplayer.EncryptedFileDataSourceFactory;
import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediacipher.helpers.VersionsUtils;
import com.google.android.exoplayer2.upstream.DataSource;
import com.liulishuo.okdownload.OkDownload;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public final class MediaCipher {

    private static volatile MediaCipher singleton;

    private final Listener listener;
    private final Config config;
    private final AESCrypter aesCrypter;
    private final OkDownload.Builder okDownloadBuilder;

    public static MediaCipher init(@Nonnull final Context applicationContext, @Nonnull final Listener listener) {
        return init(applicationContext, new Config(), null, listener);
    }

    public static MediaCipher init(@Nonnull final Context applicationContext, @Nonnull Config config, @Nonnull final Listener listener) {
        return init(applicationContext, config, null, listener);
    }

    public static MediaCipher init(@Nonnull final Context applicationContext,
                                   @Nonnull final Config config,
                                   @Nullable final OkDownload.Builder okDownloadBuilder,
                                   @Nonnull final Listener listener) {
        Checker.checkArgument(!(applicationContext instanceof Activity), "You have to pass application context instead of activity.");
        Checker.checkArgument(listener != null, "You have to pass Listener to handle keystore error. More in README");

        if (singleton == null) {
            synchronized (MediaCipher.class) {
                if (singleton == null) {
                    singleton = new MediaCipher(applicationContext, config, listener, okDownloadBuilder);
                    singleton.internalInit();
                }
            }
        }

        return singleton;
    }

    private MediaCipher(@Nonnull final Context context,
                        @Nonnull final Config config,
                        @Nonnull final Listener listener,
                        @Nullable OkDownload.Builder okDownloadBuilder) {
        this.config = config;
        this.listener = listener;
        this.okDownloadBuilder = okDownloadBuilder == null ? new OkDownload.Builder(context) : okDownloadBuilder;
        final KeysPreferences keysPreferences = new KeysPreferences(context);
        this.aesCrypter = VersionsUtils.isAtLeastMarshMallow() ?
                new AESCrypterMPlus(context, keysPreferences) : new AESCrypterBelowM(context, keysPreferences);
        aesCrypter.setListener(listener);
    }

    private void internalInit() {
        aesCrypter.init();
        initializeFileDownloadManager();
    }

    private void initializeFileDownloadManager() {
        okDownloadBuilder
                .outputStreamFactory(new DownloadEncryptingOutputStream.Factory(aesCrypter, listener));

        OkDownload.setSingletonInstance(okDownloadBuilder.build());
    }

    private static void checkInitialized() {
        Checker.checkArgument(singleton != null, "You must call init(...) method first.");
    }

    @Nonnull
    public DataSource.Factory getEncryptedFileDataSourceFactory(final File file) throws Exception {
        return new EncryptedFileDataSourceFactory(aesCrypter.getDecryptingKeys(file));
    }

    @Nonnull
    public static MediaCipher getInstance() {
        checkInitialized();
        return singleton;
    }

    @Nonnull
    public Config getConfig() {
        return config;
    }
}
