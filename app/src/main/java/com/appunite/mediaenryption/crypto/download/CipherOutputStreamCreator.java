package com.appunite.mediaenryption.crypto.download;



import com.appunite.mediaenryption.Logger;
import com.appunite.mediaenryption.crypto.AESCrypter;
import com.appunite.mediaenryption.crypto.Listener;
import com.liulishuo.filedownloader.stream.FileDownloadOutputStream;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.Nonnull;


public class CipherOutputStreamCreator implements FileDownloadHelper.OutputStreamCreator {

    @Nonnull
    private final AESCrypter aesCrypter;
    @Nonnull
    private final Listener listener;

    public CipherOutputStreamCreator(@Nonnull final AESCrypter aesCrypter,
                                     @Nonnull final Listener listener) {
        this.aesCrypter = aesCrypter;
        this.listener = listener;
    }

    @Override
    public FileDownloadOutputStream create(final File file) throws FileNotFoundException {
        try {
            return new CryptoFileDownloadOutputStream(aesCrypter, file);
        } catch (Exception e) {
            listener.onError(e);
            Logger.logError("Cannot create FileDownloadOutputStream with error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean supportSeek() {
        return true;
    }
}
