package com.appunite.mediacipher.crypto.exoplayer;


import android.net.Uri;

import com.appunite.mediacipher.helpers.Logger;
import com.appunite.mediacipher.crypto.AESCrypter;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.crypto.CipherInputStream;


public class EncryptedFileDataSource implements DataSource {
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    private final AESCrypter aesCrypter;
    private CipherInputStream cipherInputStream;
    private Uri uri;
    private long bytesRemaining;
    private boolean opened;

    public EncryptedFileDataSource(AESCrypter aesCrypter) {
        this.aesCrypter = aesCrypter;
    }

    @Override
    public long open(DataSpec dataSpec) throws EncryptedFileDataSourceException {
        Logger.logDebug("open with length: " + dataSpec.length + "    " + this.toString());
        // if we're open, we shouldn't need to open again, fast-fail
        if (opened) {
            return bytesRemaining;
        }

        uri = dataSpec.uri;

        try {
            setupInputStream();
            skipToPosition(dataSpec);
            computeBytesRemaining(dataSpec);
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        } catch (Exception e) {
            Logger.logError("Cannot open data source");
        }

        opened = true;

        Logger.logDebug("bytesRemaining: " + bytesRemaining + "    " + this.toString());

        return bytesRemaining;
    }

    private void setupInputStream() throws Exception {
        final File encryptedFile = new File(uri.getPath());
        final FileInputStream fileInputStream = new FileInputStream(encryptedFile);
        cipherInputStream = aesCrypter.getDecryptingStream(fileInputStream);
    }

    private void skipToPosition(DataSpec dataSpec) throws IOException {
        workaroundSkip(dataSpec.position);
    }

    private void computeBytesRemaining(DataSpec dataSpec) throws IOException {
        if (dataSpec.length != C.LENGTH_UNSET) {
            bytesRemaining = dataSpec.length;
        } else {
            bytesRemaining = cipherInputStream.available();
            if (bytesRemaining == Integer.MAX_VALUE) {
                bytesRemaining = C.LENGTH_UNSET;
            }
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        // fast-fail if there's 0 quantity requested or we think we've already processed everything
        if (readLength == 0) {
            return 0;
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        }
        // constrain the read length and try to read from the cipher input stream
        int bytesToRead = getBytesToRead(readLength);
        int bytesRead;
        try {
            bytesRead = cipherInputStream.read(buffer, offset, bytesToRead);
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        }
        // if we get a -1 that means we failed to read - we're either going to EOF error or broadcast EOF
        if (bytesRead == -1) {
            if (bytesRemaining != C.LENGTH_UNSET) {
                throw new EncryptedFileDataSourceException(new EOFException());
            }
            return C.RESULT_END_OF_INPUT;
        }
        // we can't decrement bytes remaining if it's just a flag representation (as opposed to a mutable numeric quantity)
        if (bytesRemaining != C.LENGTH_UNSET) {
            bytesRemaining -= bytesRead;
        }
        // report
        return bytesRead;
    }

    private int getBytesToRead(int bytesToRead) {
        if (bytesRemaining == C.LENGTH_UNSET) {
            return bytesToRead;
        }
        return (int) Math.min(bytesRemaining, bytesToRead);
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws EncryptedFileDataSourceException {
        Logger.logDebug("close " + this.toString());
        uri = null;
        try {
            if (cipherInputStream != null) {
                cipherInputStream.close();
            }
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        } finally {
            cipherInputStream = null;
            if (opened) {
                opened = false;
            }
        }
    }

    public static final class EncryptedFileDataSourceException extends IOException {
        public EncryptedFileDataSourceException(IOException cause) {
            super(cause);
        }
    }

    public long workaroundSkip(long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        int read;

        if (bytesToSkip <= 0) {
            return 0;
        }

        int size = (int) Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            read = cipherInputStream.read(skipBuffer, 0, (int) Math.min(size, remaining));
            if (read < 0) {
                break;
            }
            remaining -= read;
        }

        return bytesToSkip - remaining;
    }
}

