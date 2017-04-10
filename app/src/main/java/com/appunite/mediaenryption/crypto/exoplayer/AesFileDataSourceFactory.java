package com.appunite.mediaenryption.crypto.exoplayer;


import android.support.annotation.NonNull;

import com.appunite.mediaenryption.crypto.AESCrypter;
import com.google.android.exoplayer2.upstream.DataSource;


public class AesFileDataSourceFactory implements DataSource.Factory {


    @NonNull
    private final AESCrypter aesCrypter;

    public AesFileDataSourceFactory(@NonNull final AESCrypter aesCrypter) {
        this.aesCrypter = aesCrypter;
    }

    @Override
    public DataSource createDataSource() {
        return new AesFileDataSource(aesCrypter);
    }
}
