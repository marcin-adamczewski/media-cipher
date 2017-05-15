package com.appunite.mediacipher;


import android.content.Context;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.TrayStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KeysPreferences extends TrayPreferences {

    private final static String KEY_KEY_ALIAS = "key_alias";
    private final static String KEY_ENCRYPTED_AES_KEY = "encrypted_aes_key";

    private final AppPreferences preferences;

    public KeysPreferences(@Nonnull final Context context) {
        super(context, "keyPresModule", 1, TrayStorage.Type.DEVICE);
        preferences = new AppPreferences(context);
    }

    @Nullable
    public String getKeyAlias() {
        return preferences.getString(KEY_KEY_ALIAS, null);
    }

    @Nullable
    public String getEncryptedAESKey() {
        return preferences.getString(KEY_ENCRYPTED_AES_KEY, null);
    }

    public void setKeyAlias(@Nonnull String alias) {
        preferences.put(KEY_KEY_ALIAS, alias);
    }

    public void setEncryptedAESKey(@Nonnull String encryptedAESKey) {
        preferences.put(KEY_ENCRYPTED_AES_KEY, encryptedAESKey);
    }
}

