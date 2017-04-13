package com.appunite.mediacipher.crypto.exoplayer;


import com.appunite.mediacipher.crypto.DecryptingKeys;
import com.google.android.exoplayer2.upstream.DataSource;


public class EncryptedFileDataSourceFactory implements DataSource.Factory {

    private final DecryptingKeys decryptingKeys;

    public EncryptedFileDataSourceFactory(DecryptingKeys decryptingKeys) {
        this.decryptingKeys = decryptingKeys;
    }

    @Override
    public EncryptedFileDataSource createDataSource() {
        return new EncryptedFileDataSource(decryptingKeys);
    }

}
