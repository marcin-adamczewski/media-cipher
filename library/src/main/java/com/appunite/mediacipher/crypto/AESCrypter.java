package com.appunite.mediacipher.crypto;


import android.content.Context;

import com.appunite.mediacipher.KeysPreferences;
import com.appunite.mediacipher.Listener;
import com.appunite.mediacipher.crypto.download.InitVectorCipherOutputStream;
import com.appunite.mediacipher.helpers.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public abstract class AESCrypter {

    private static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";
    private static final int INIT_VECTOR_SIZE = 16;
    protected static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    protected final Context context;
    protected final KeysPreferences keysPreferences;
    protected KeyStore keyStore;

    private boolean isInitialized;
    private Listener listener;

    public AESCrypter(@Nonnull Context context, @Nonnull KeysPreferences keysPreferences) {
        this.context = context;
        this.keysPreferences = keysPreferences;
    }

    @Nonnull
    protected abstract SecretKey getAESKey(@Nonnull String keyAlias) throws Exception;

    @Nonnull
    protected abstract SecretKey generateNewAESKey(@Nonnull String keyAlias) throws Exception;

    public void init() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            isInitialized = true;
        } catch (Exception e) {
            listener.onError(e);
        }
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    @Nonnull
    public synchronized CipherOutputStream getEncryptingStream(@Nonnull final OutputStream outputStream) throws Exception {
        checkInitialized();

        final String keyAlias = keysPreferences.getKeyAlias();

        try {
            final SecretKey secretKey = getOrCreateAesKey(keyAlias);
            return getCipherOutputStream(outputStream, secretKey);
        } catch (Exception e) {
            onError(e);
            removeKeyIfCorruptedAndGenerateNewOne(e);
            final SecretKey secretKey = getAESKey(keyAlias);
            Logger.logDebug("Refreshed key fetched");
            return getCipherOutputStream(outputStream, secretKey);
        }
    }

    @Nonnull
    public synchronized DecryptingKeys getDecryptingKeys(final File encryptedFile) throws Exception {
        checkInitialized();

        final String keyAlias = keysPreferences.getKeyAlias();

        try {
            final SecretKey secretKey = getOrCreateAesKey(keyAlias);

            final InitVectorFileInputStream iVFileInputStream = new InitVectorFileInputStream(encryptedFile);
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(iVFileInputStream.getInitVector());
            iVFileInputStream.close();

            final Cipher decryptCipher = Cipher.getInstance(AES_TRANSFORMATION);
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            return new DecryptingKeys(secretKey, ivParameterSpec, decryptCipher);
        } catch (Exception e) {
            onError(e);
            removeKeyIfCorruptedAndGenerateNewOne(e);
            throw e;
        }
    }

    @Nonnull
    private CipherOutputStream getCipherOutputStream(@Nonnull final OutputStream outputStream,
                                                     @Nonnull final SecretKey secretKey) throws Exception {
        final byte[] initVector = generateInitializationVector();

        final Cipher encryptCipher = Cipher.getInstance(AES_TRANSFORMATION);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(initVector));

        return new InitVectorCipherOutputStream(outputStream, encryptCipher, initVector);
    }

    @Nonnull
    private SecretKey getOrCreateAesKey(@Nullable final String keyAlias) throws Exception {
        if (keyAlias == null) {
            Logger.logDebug("keyAlias null");
            return generateAndStoreNewAESKey();
        } else {
            Logger.logDebug("existing key fetched: ");
            return getAESKey(keyAlias);
        }
    }

    private void removeKeyIfCorruptedAndGenerateNewOne(final Exception error) throws Exception {
        if (!isKeyCorrupted(error)) {
            keysPreferences.clear();
            tryRefreshKey();
        }
    }

    // There is a lot of different exceptions that may cause key to be corrupted so it is hard to check all cases.
    // More here https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.on3ZPwjc.6LLYdFom.dpbs
    private boolean isKeyCorrupted(Throwable throwable) {
        return !(throwable instanceof IOException);
    }

    private void tryRefreshKey() throws Exception {
        generateAndStoreNewAESKey();
        Logger.logDebug("key refreshed: ");
    }

    @Nonnull
    private String generateKeyAlias() {
        return String.valueOf(new SecureRandom().nextInt());
    }

/*
    private synchronized void generateKeyIfNeeded() throws Exception {
        if (keysPreferences.getKeyAlias() == null) {
            generateAndStoreNewAESKey();
        }
    }
*/

    @Nonnull
    private SecretKey generateAndStoreNewAESKey() throws Exception {
        Logger.logDebug("Generating new aes key");

        final String keyAlias = generateKeyAlias();
        final SecretKey secretKey = generateNewAESKey(keyAlias);
        keysPreferences.setKeyAlias(keyAlias);

        Logger.logDebug("New aes key generated for keyAlias: " + keyAlias);

        return secretKey;
    }

    private void onError(final Exception e) {
        Logger.logError("exception  " + e.getMessage());
        if (listener != null) {
            if (isKeyCorrupted(e)) {
                listener.onKeyCorrupted(e);
            } else {
                listener.onError(e);
            }
        }
    }

    private void checkInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("AesCrypter not initialized. Call init method");
        }
    }

    private byte[] generateInitializationVector() {
        final byte[] bytesToFill = new byte[INIT_VECTOR_SIZE];
        new SecureRandom().nextBytes(bytesToFill);
        return bytesToFill;
    }

}
