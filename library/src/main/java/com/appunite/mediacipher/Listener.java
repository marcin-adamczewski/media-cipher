package com.appunite.mediacipher;


public interface Listener {
    void onKeystoreError(Throwable throwable);
    void onError(Throwable throwable);
}
