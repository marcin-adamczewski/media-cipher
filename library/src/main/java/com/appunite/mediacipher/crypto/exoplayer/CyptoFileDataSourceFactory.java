package com.appunite.mediacipher.crypto.exoplayer;


import com.appunite.mediacipher.crypto.DecryptingKeys;
import com.google.android.exoplayer2.upstream.DataSource;


public class CyptoFileDataSourceFactory implements DataSource.Factory {

    private final DecryptingKeys decryptingKeys;

    public CyptoFileDataSourceFactory(DecryptingKeys decryptingKeys) {
        this.decryptingKeys = decryptingKeys;
    }

    @Override
    public CryptoFileDataSource createDataSource() {
        return new CryptoFileDataSource(decryptingKeys);
    }

}
