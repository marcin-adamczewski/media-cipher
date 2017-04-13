package com.appunite.mediacipher.crypto;


import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class DecryptingKeys {
    @Nonnull
    private final SecretKey secretKey;
    @Nonnull
    private final IvParameterSpec ivParameterSpec;
    @Nonnull
    private final Cipher cipher;

    public DecryptingKeys(@Nonnull final SecretKey secretKey,
                          @Nonnull final IvParameterSpec ivParameterSpec,
                          @Nonnull final Cipher cipher) {
        this.secretKey = secretKey;
        this.ivParameterSpec = ivParameterSpec;
        this.cipher = cipher;
    }

    @Nonnull
    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Nonnull
    public IvParameterSpec getIvParameterSpec() {
        return ivParameterSpec;
    }

    @Nonnull
    public Cipher getCipher() {
        return cipher;
    }
}
