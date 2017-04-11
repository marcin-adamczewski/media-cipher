package com.appunite.mediacipher.crypto;


import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

/**
 * CipherOutputStream that writes initialization vector at the beginning of stream
 * This vector is needed for decryption.
 */
public class InitVectorCipherOutputStream extends CipherOutputStream {

    public InitVectorCipherOutputStream(final OutputStream os, final Cipher cipher, final byte[] initVector) throws IOException {
        super(os, cipher);
        os.write(initVector);
    }
}
