package com.appunite.mediaenryption.crypto.download;


import android.support.annotation.NonNull;

import com.appunite.mediaenryption.crypto.AESCrypter;
import com.liulishuo.filedownloader.stream.FileDownloadOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.crypto.CipherOutputStream;

public class CryptoFileDownloadOutputStream implements FileDownloadOutputStream {

    private final CipherOutputStream cipherOutputStream;
    private final RandomAccessFile accessFile;

    public CryptoFileDownloadOutputStream(@NonNull AESCrypter aesCrypter, @NonNull File file) throws Exception {
        cipherOutputStream = aesCrypter.getEncryptStream(new FileOutputStream(file));
        accessFile = new RandomAccessFile(file, "rw");
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        cipherOutputStream.write(b, off, len);
    }

    @Override
    public void sync() throws IOException {
        accessFile.getFD().sync();
    }

    @Override
    public void close() throws IOException {
        cipherOutputStream.close();
        accessFile.close();
    }

    @Override
    public void seek(final long offset) throws IOException, IllegalAccessException {
        accessFile.seek(offset);
    }

    @Override
    public void setLength(final long newLength) throws IOException, IllegalAccessException {
        accessFile.setLength(newLength);
    }
}
