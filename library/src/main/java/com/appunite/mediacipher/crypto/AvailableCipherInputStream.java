package com.appunite.mediacipher.crypto;


import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class AvailableCipherInputStream extends CipherInputStream {

    public AvailableCipherInputStream(final InputStream is, final Cipher cipher) throws IOException {
        super(is, cipher);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }
}
