package com.appunite.mediacipher;


public interface Listener {

    /** When this error happens you probably want to remove all your already encrypted content as key is no more valid **/
    void onKeyCorrupted(Throwable throwable);

    void onError(Throwable throwable);
}
