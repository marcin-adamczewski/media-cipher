package com.appunite.mediacipher.crypto;


import com.appunite.mediacipher.helpers.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * CipherInputStream that reads initialization vector from first $initVectorSize bytes so Cipher can be initialized
 * with this vector and inputStream offset will be increased so initialization vector won't be considered while decrypting data
 */
public class InitVectorFileInputStream extends FileInputStream {

    private final static int DEFAULT_INIT_VECTOR_SIZE = 16;

    private final byte[] initVector;

    public InitVectorFileInputStream(final File file, int initVectorSize) throws IOException {
        super(file);

        initVector = new byte[initVectorSize];
        final int bytesRead = read(initVector);
        if (bytesRead != initVectorSize) {
            Logger.logError("Cannot read init vector from file");
        }
    }

    public InitVectorFileInputStream(final File file) throws IOException {
        this(file, DEFAULT_INIT_VECTOR_SIZE);
    }

    public byte[] getInitVector() {
        return initVector;
    }

}
