package com.appunite.mediacipher.crypto;




import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;


/**
 * CipherInputStream that reads initialization vector from first $initVectorSize bytes so Cipher can be initialized
 * with this vector and stream offset will be changed so initialization vector won't be considered while decrypting data
 */
public class InitVectorCipherInputStream extends CipherInputStream {

    private final byte[] initVector;

    public InitVectorCipherInputStream(final InputStream is, final Cipher cipher, int initVectorSize) throws IOException {
        super(is, cipher);

        initVector = new byte[initVectorSize];
        final int bytesRead = is.read(initVector);
        if (bytesRead != initVectorSize) {
            throw new IOException("read bytes: " + bytesRead + " doesn't equal to init vector size: " + initVectorSize);
        }
    }

    public byte[] getInitVector() {
        return initVector;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }
}
