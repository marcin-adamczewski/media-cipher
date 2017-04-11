package com.appunite.mediaenryption;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.appunite.mediaenryption.crypto.AESCrypter;
import com.appunite.mediaenryption.crypto.AESCrypterBelowM;
import com.appunite.mediaenryption.crypto.AESCrypterMPlus;
import com.appunite.mediaenryption.crypto.Listener;
import com.appunite.mediaenryption.crypto.download.CipherOutputStreamCreator;
import com.appunite.mediaenryption.crypto.exoplayer.EncryptedFileDataSourceFactory;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;

import javax.annotation.Nonnull;

public final class MediaEncrypter {

    private static volatile MediaEncrypter singleton;

    private final Context context;
    private final Listener listener;
    private final Config config;
    private final AESCrypter aesCrypter;

    public static MediaEncrypter init(@Nonnull final Context applicationContext) {
        return init(applicationContext, new Config(), null);
    }

    public static MediaEncrypter init(@Nonnull final Context applicationContext,
                                      @Nullable final Config config,
                                      @Nullable final Listener listener) {
        if (applicationContext instanceof Activity) {
            throw new IllegalStateException("You have to pass application context instead of activity.");
        }

        if (singleton == null) {
            synchronized (MediaEncrypter.class) {
                if (singleton == null) {
                    singleton = new MediaEncrypter(
                            applicationContext,
                            config == null ? new Config() : config,
                            listener);
                    singleton.internalInit();
                }
            }
        }

        return singleton;
    }

    private MediaEncrypter(final Context context, final Config config, final Listener listener) {
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
    public static MediaEncrypter getInstance() {
        Checker.checkArgument(singleton != null, "You must call init(...) method first.");
        return singleton;
    }

    @Nonnull
    public Config getConfig() {
        return config;
    }
}
