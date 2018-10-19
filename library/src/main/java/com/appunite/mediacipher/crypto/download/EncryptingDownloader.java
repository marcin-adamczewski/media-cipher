package com.appunite.mediacipher.crypto.download;

import com.appunite.mediacipher.Listener;
import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.helpers.Logger;
import com.tonyodev.fetch2core.OutputResourceWrapper;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;

public class EncryptingDownloader extends OkHttpDownloader {

    @Nonnull private AESCrypter aesCrypter;
    @Nonnull private Listener listener;

    public EncryptingDownloader(@Nonnull OkHttpClient okHttpClient,
                                @Nonnull AESCrypter aesCrypter,
                                @Nonnull Listener listener) {
        super(okHttpClient, FileDownloaderType.SEQUENTIAL);
        this.aesCrypter = aesCrypter;
        this.listener = listener;
    }

    @Nullable
    @Override
    public OutputResourceWrapper getRequestOutputResourceWrapper(@NotNull ServerRequest request) {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(
                    request.getFile(), false);
            return new MyOutResourceWrapper(aesCrypter.getEncryptingStream(fileOutputStream));
        } catch (Exception e) {
            listener.onError(e);
            Logger.logError("Cannot create EncryptingDownloader with error: " + e.getMessage());
            return null;
        }
    }
}

class MyOutResourceWrapper extends OutputResourceWrapper {

    private final OutputStream stream;

    MyOutResourceWrapper(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void setWriteOffset(long l) throws IOException {

    }

    @Override
    public void write(@NotNull byte[] bytes, int off, int len) throws IOException {
        stream.write(bytes, off, len);
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}