package com.appunite.mediacipher.crypto.download;

import com.appunite.mediacipher.Listener;
import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.helpers.Logger;
import com.tonyodev.fetch2downloaders.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;

public class EncryptingDownloader extends OkHttpDownloader {

    @Nonnull private AESCrypter aesCrypter;
    @Nonnull private Listener listener;

    public EncryptingDownloader(@Nonnull OkHttpClient okHttpClient,
                                @Nonnull AESCrypter aesCrypter,
                                @Nonnull Listener listener) {
        super(okHttpClient);
        this.aesCrypter = aesCrypter;
        this.listener = listener;
    }

    @Nullable
    @Override
    public OutputStream getRequestOutputStream(@NotNull Request request, long filePointerOffset) {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(
                    request.getFile(), false);
            return aesCrypter.getEncryptingStream(fileOutputStream);
        } catch (Exception e) {
            listener.onError(e);
            Logger.logError("Cannot create EncryptingDownloader with error: " + e.getMessage());
            return null;
        }
    }

    @NotNull
    @Override
    public FileDownloaderType getFileDownloaderType(Request request) {
        return FileDownloaderType.SEQUENTIAL;
    }
}