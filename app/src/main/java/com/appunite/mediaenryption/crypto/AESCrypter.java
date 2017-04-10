package com.appunite.mediaenryption.crypto;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.appunite.mediaenryption.KeysPreferences;
import com.appunite.mediaenryption.LogHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

public abstract class AESCrypter {
    private static final String TAG = AESCrypter.class.getSimpleName();

    protected static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    protected KeyStore keyStore;
    @NonNull
    protected final Context context;
    @NonNull
    protected final KeysPreferences keysPreferences;

    private boolean isInitialized;
    private Listener listener;

    public AESCrypter(@NonNull Context context, @NonNull KeysPreferences keysPreferences) {
        this.context = context;
        this.keysPreferences = keysPreferences;
    }

    public void init() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            isInitialized = true;
            generateKeyIfNeeded();
        } catch (Exception e) {
            onError(e);
        }
    }

    private void generateKeyIfNeeded() throws Exception {
        if (keysPreferences.getKeyAlias() == null) {
            generateAndStoreNewAESKey();
        }
    }

    @NonNull
    protected abstract CipherOutputStream getCipherOutputStream(@NonNull final OutputStream outputStream, @NonNull final SecretKey secretKey) throws Exception;

    @NonNull
    protected abstract CipherInputStream getCipherInputStream(@NonNull final InputStream inputStream, @NonNull final SecretKey secretKey) throws Exception;

    @Nonnull
    public synchronized CipherOutputStream getEncryptStream(@NonNull final OutputStream outputStream) throws Exception {
        checkInitialized();

        final String keyAlias = keysPreferences.getKeyAlias();

        try {
            final SecretKey secretKey = getOrCreateAesKey(keyAlias);
            return getCipherOutputStream(outputStream, secretKey);
        } catch (Exception e) {
            onError(e);
            clearPreferences();
            tryRefreshKey();
            final SecretKey secretKey = getAESKey(keyAlias);
            log("refreshed key fetched: ");
            return getCipherOutputStream(outputStream, secretKey);
        }
    }

    @Nonnull
    public synchronized CipherInputStream getDecryptStream(@NonNull final InputStream inputStream) throws Exception {
        checkInitialized();

        final String keyAlias = keysPreferences.getKeyAlias();
        try {
            final SecretKey secretKey = getOrCreateAesKey(keyAlias);
            return getCipherInputStream(inputStream, secretKey);
        } catch (Exception e) {
            onError(e);
            clearPreferences();
            tryRefreshKey();

            throw e;
        }
    }

    @NonNull
    private SecretKey getOrCreateAesKey(@Nullable final String keyAlias) throws Exception {
        if (keyAlias == null) {
            log("keyAlias null");
            return generateAndStoreNewAESKey();
        } else {
            log("existing key fetched: ");
            return getAESKey(keyAlias);
        }
    }

    private void clearPreferences() {
        keysPreferences.edit().setKeyAlias(null);
        keysPreferences.edit().setEncryptedAESKey(null);
    }

    private void tryRefreshKey() throws Exception {
        generateAndStoreNewAESKey();
        log("key refreshed: ");
    }

    @NonNull
    protected abstract SecretKey getAESKey(@NonNull String keyAlias) throws Exception;

    @NonNull
    protected abstract SecretKey generateNewAESKey(@NonNull String keyAlias) throws Exception;

    @NonNull
    protected String generateKeyAlias() {
        return String.valueOf(new SecureRandom().nextInt());
    }

    @NonNull
    private SecretKey generateAndStoreNewAESKey() throws Exception {
        log("Generating new key");
        final String keyAlias = generateKeyAlias();

        final SecretKey secretKey = generateNewAESKey(keyAlias);
        keysPreferences.edit().setKeyAlias(keyAlias);

        log("New key generated");

        return secretKey;
    }

    private void log(String message) {
        LogHelper.logIfDebug(TAG, message);
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onKeystoreError(Throwable throwable);
    }

    private void onError(final Exception e) {
        log("exception  " + e.getMessage());
        if (listener != null) {
            listener.onKeystoreError(e);
        }
    }

    private void checkInitialized() {
        if (!isInitialized) {
            throw new RuntimeException("AesCrypter not initialized. Call init method");
        }
    }

}
