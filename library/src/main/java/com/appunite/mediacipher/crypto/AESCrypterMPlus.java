package com.appunite.mediacipher.crypto;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.appunite.mediacipher.KeysPreferences;
import com.appunite.mediacipher.helpers.Checker;

import java.security.KeyStore;

import javax.annotation.Nonnull;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
public class AESCrypterMPlus extends AESCrypter {

    public AESCrypterMPlus(@Nonnull final Context context, @Nonnull final KeysPreferences keysPreferences) {
        super(context, keysPreferences);
    }

    @Nonnull
    @Override
    protected SecretKey getAESKey(@Nonnull final String keyAlias) throws Exception {
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null);
        Checker.checkNotNull(secretKeyEntry, "Key entry is null for alias: " + keyAlias);
        return secretKeyEntry.getSecretKey();
    }

    @Nonnull
    @Override
    protected SecretKey generateNewAESKey(@Nonnull String keyAlias) throws Exception {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CTR)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build());
        return keyGenerator.generateKey();
    }
}
