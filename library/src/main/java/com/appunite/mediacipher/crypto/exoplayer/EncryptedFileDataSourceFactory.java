package com.appunite.mediacipher.crypto.exoplayer;



import com.appunite.mediacipher.crypto.AESCrypter;
import com.google.android.exoplayer2.upstream.DataSource;

import javax.annotation.Nonnull;


public class EncryptedFileDataSourceFactory implements DataSource.Factory {

    @Nonnull
    private final AESCrypter aesCrypter;

    public EncryptedFileDataSourceFactory(@Nonnull final AESCrypter aesCrypter) {
        this.aesCrypter = aesCrypter;
    }

    @Override
    public DataSource createDataSource() {
        return new EncryptedFileDataSource(aesCrypter);
    }
}
