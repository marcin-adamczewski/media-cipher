package com.appunite.mediacipher;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KeysPreferences {

    private final static String PREFERENCES_NAME = "keys_preferences";

    private final static String KEY_KEY_ALIAS = "key_alias";
    private final static String KEY_ENCRYPTED_AES_KEY = "encrypted_aes_key";

    private final SharedPreferences preferences;
    private final Editor editor;

    public KeysPreferences(@Nonnull final Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = new Editor();
    }

    @Nullable
    public String getKeyAlias() {
        return preferences.getString(KEY_KEY_ALIAS, null);
    }

    @Nullable
    public String getEncryptedAESKey() {
        return preferences.getString(KEY_ENCRYPTED_AES_KEY, null);
    }

    public class Editor {
        private final SharedPreferences.Editor edit;

        @SuppressLint("CommitPrefEdits")
        public Editor() {
            edit = preferences.edit();
        }

        public void setKeyAlias(@Nonnull String alias) {
            edit.putString(KEY_KEY_ALIAS, alias).apply();
        }

        public void setEncryptedAESKey(@Nonnull String encryptedAESKey) {
            edit.putString(KEY_ENCRYPTED_AES_KEY, encryptedAESKey).apply();
        }
    }

    public Editor edit() {
        return editor;
    }
}

