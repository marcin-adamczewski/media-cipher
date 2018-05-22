package com.appunite.mediacipher.crypto.download;


import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

/**
 * CipherOutputStream that writes initialization vector at the beginning of stream
 * This vector is needed for decryption.
 */
public class InitVectorCipherOutputStream extends CipherOutputStream {

    private final byte[] initVector;
    private boolean firstWrite = true;

    public InitVectorCipherOutputStream(final OutputStream os,
                                        final Cipher cipher,
                                        final byte[] initVector) {
        super(os, cipher);
        this.initVector = initVector;
    }

    @Override
    public void write(int b) throws IOException {
        writeInitVectorIfNeeded();
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        writeInitVectorIfNeeded();
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeInitVectorIfNeeded();
        super.write(b, off, len);
    }

    private void writeInitVectorIfNeeded() throws IOException {
        if (firstWrite) {
            out.write(initVector);
            firstWrite = false;
        }
    }
}
