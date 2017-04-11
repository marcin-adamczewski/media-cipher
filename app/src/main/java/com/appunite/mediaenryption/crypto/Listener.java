package com.appunite.mediaenryption.crypto;


public interface Listener {
    void onKeystoreError(Throwable throwable);
    void onError(Throwable throwable);
}
