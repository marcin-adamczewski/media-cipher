package com.appunite.mediacipher;


import android.app.Activity;
import android.content.Context;

import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.crypto.AESCrypterBelowM;
import com.appunite.mediacipher.crypto.AESCrypterMPlus;
import com.appunite.mediacipher.crypto.download.EncryptingOutputStreamCreator;
import com.appunite.mediacipher.crypto.exoplayer.EncryptedFileDataSourceFactory;
import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediacipher.helpers.Logger;
import com.appunite.mediacipher.helpers.VersionsUtils;
import com.google.android.exoplayer2.upstream.DataSource;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public final class MediaCipher {

    private static volatile MediaCipher singleton;

    private final Context context;
    @Nullable
    private final Listener listener;
    private final Config config;
    private final AESCrypter aesCrypter;
    private final DownloadMgrInitialParams.InitCustomMaker initCustomMaker;

    public static MediaCipher init(@Nonnull final Context applicationContext, @Nonnull final Listener listener) {
        return init(applicationContext, new Config(), listener, null);
    }

    public static MediaCipher init(@Nonnull final Context applicationContext, @Nonnull Config config, @Nonnull final Listener listener) {
        return init(applicationContext, config, listener, null);
    }

    public static MediaCipher init(@Nonnull final Context applicationContext,
                                   @Nonnull final Config config,
                                   @Nonnull final Listener listener,
                                   @Nullable final DownloadMgrInitialParams.InitCustomMaker initCustomMaker) {
        Checker.checkArgument(!(applicationContext instanceof Activity), "You have to pass application context instead of activity.");
        Checker.checkArgument(listener != null, "You have to pass Listener to handle keystore error. More in README");

        if (singleton == null) {
            synchronized (MediaCipher.class) {
                if (singleton == null) {
                    singleton = new MediaCipher(applicationContext, config, listener, initCustomMaker);
                    singleton.internalInit();
                }
            }
        }

        return singleton;
    }

    private MediaCipher(@Nonnull final Context context,
                        @Nonnull final Config config,
                        @Nonnull final Listener listener,
                        @Nullable DownloadMgrInitialParams.InitCustomMaker initCustomMaker) {
        this.context = context;
        this.config = config;
        this.listener = listener;
        this.initCustomMaker = initCustomMaker == null ? new DownloadMgrInitialParams.InitCustomMaker() : initCustomMaker;
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
            initCustomMaker.outputStreamCreator(new EncryptingOutputStreamCreator(aesCrypter, listener));
            FileDownloader.init(context, initCustomMaker);
        } catch (Exception e) {
            Logger.logError("Cannot initialize file downloader: " + e.getMessage());
            listener.onError(e);
        }
    }

    @Nonnull
    public DataSource.Factory getEncryptedFileDataSourceFactory(final File file) throws Exception {
        return new EncryptedFileDataSourceFactory(aesCrypter.getDecryptingKeys(file));
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
