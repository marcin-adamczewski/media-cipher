package com.appunite.mediacipher;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.crypto.AESCrypterBelowM;
import com.appunite.mediacipher.crypto.AESCrypterMPlus;
import com.appunite.mediacipher.crypto.download.CipherOutputStreamCreator;
import com.appunite.mediacipher.crypto.exoplayer.EncryptedFileDataSourceFactory;
import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediacipher.helpers.Logger;
import com.appunite.mediacipher.helpers.VersionsUtils;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;

import javax.annotation.Nonnull;


public final class MediaCipher {

    private static volatile MediaCipher singleton;

    private final Context context;
    private final Listener listener;
    private final Config config;
    private final AESCrypter aesCrypter;

    public static MediaCipher init(@Nonnull final Context applicationContext) {
        return init(applicationContext, new Config(), null);
    }

    public static MediaCipher init(@Nonnull final Context applicationContext,
                                   @Nullable final Config config,
                                   @Nullable final Listener listener) {
        if (applicationContext instanceof Activity) {
            throw new IllegalStateException("You have to pass application context instead of activity.");
        }

        if (singleton == null) {
            synchronized (MediaCipher.class) {
                if (singleton == null) {
                    singleton = new MediaCipher(
                            applicationContext,
                            config == null ? new Config() : config,
                            listener);
                    singleton.internalInit();
                }
            }
        }

        return singleton;
    }

    private MediaCipher(final Context context, final Config config, final Listener listener) {
        this.context = context;
        this.config = config;
        this.listener = listener;
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
        try {
            final DownloadMgrInitialParams.InitCustomMaker initCustomMaker = new DownloadMgrInitialParams.InitCustomMaker();
            initCustomMaker.outputStreamCreator(new CipherOutputStreamCreator(aesCrypter, listener));

            FileDownloader.init(context, initCustomMaker);
        } catch (Exception e) {
            Logger.logError("Cannot initialize file downloader: " + e.getMessage());
            listener.onError(e);
        }
    }

    @Nonnull
    public EncryptedFileDataSourceFactory getEncryptedFileDataSourceFactory() {
        return new EncryptedFileDataSourceFactory(aesCrypter);
    }

    @Nonnull
    public static MediaCipher getInstance() {
        Checker.checkArgument(singleton != null, "You must call init(...) method first.");
        return singleton;
    }

    @Nonnull
    public Config getConfig() {
        return config;
    }
}
