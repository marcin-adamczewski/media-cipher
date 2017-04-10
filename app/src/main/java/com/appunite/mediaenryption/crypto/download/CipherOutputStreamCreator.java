package com.appunite.mediaenryption.crypto.download;


import android.support.annotation.NonNull;

import com.appunite.mediaenryption.LogHelper;
import com.appunite.mediaenryption.crypto.AESCrypter;
import com.liulishuo.filedownloader.stream.FileDownloadOutputStream;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

import java.io.File;
import java.io.FileNotFoundException;


public class CipherOutputStreamCreator implements FileDownloadHelper.OutputStreamCreator {

    @NonNull
    private final AESCrypter aesCrypter;

    public CipherOutputStreamCreator(@NonNull final AESCrypter aesCrypter) {
        this.aesCrypter = aesCrypter;
    }

    @Override
    public FileDownloadOutputStream create(final File file) throws FileNotFoundException {
        try {
            return new CryptoFileDownloadOutputStream(aesCrypter, file);
        } catch (Exception e) {
            LogHelper.logIfDebug("Cannot create FileDownloadOutputStream with error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean supportSeek() {
        return true;
    }
}
