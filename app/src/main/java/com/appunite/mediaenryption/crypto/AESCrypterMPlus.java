package com.appunite.mediaenryption.crypto;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;

import com.appunite.mediaenryption.KeysPreferences;
import com.appunite.mediaenryption.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@TargetApi(Build.VERSION_CODES.M)
public class AESCrypterMPlus extends AESCrypter {

    private static final String AES_MODE = "AES/CTR/NoPadding";
    private static final int INIT_VECTOR_SIZE = 16;

    public AESCrypterMPlus(@NonNull final Context context, @NonNull final KeysPreferences keysPreferences) {
        super(context, keysPreferences);
    }

    @NonNull
    @Override
    protected CipherOutputStream getCipherOutputStream(@NonNull final OutputStream outputStream,
                                                       @NonNull final SecretKey secretKey) throws Exception {
        Logger.logDebug("getCipherOutputStream " + this.toString());
        final byte[] initVector = new byte[INIT_VECTOR_SIZE];
        generateInitializationVector(initVector);

        final Cipher encryptCipher = Cipher.getInstance(AES_MODE);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(initVector));

        return new InitVectorCipherOutputStream(outputStream, encryptCipher, initVector);
    }

    @NonNull
    @Override
    protected CipherInputStream getCipherInputStream(@NonNull final InputStream inputStream,
                                                     @NonNull final SecretKey secretKey) throws Exception {
        Logger.logDebug("getCipherInputStream " + this.toString());
        final Cipher decryptCipher = Cipher.getInstance(AES_MODE);

        final InitVectorCipherInputStream cipherInputStream = new InitVectorCipherInputStream(inputStream, decryptCipher, INIT_VECTOR_SIZE);
        final byte[] initVector = cipherInputStream.getInitVector();
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initVector));

        return cipherInputStream;
    }

    @NonNull
    @Override
    protected SecretKey getAESKey(@NonNull final String keyAlias) throws Exception {
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null);
        return secretKeyEntry.getSecretKey();
    }

    @NonNull
    @Override
    protected SecretKey generateNewAESKey(@NonNull String keyAlias) throws Exception {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CTR)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build());
        return keyGenerator.generateKey();
    }

    private void generateInitializationVector(byte[] bytesToFill) {
        new SecureRandom().nextBytes(bytesToFill);
    }
}
