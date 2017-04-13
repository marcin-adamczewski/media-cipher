package com.appunite.mediacipher.crypto.exoplayer;


import android.net.Uri;

import com.appunite.mediacipher.crypto.DecryptingKeys;
import com.appunite.mediacipher.crypto.InitVectorFileInputStream;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public final class CryptoFileDataSource implements DataSource {

    private StreamingCipherInputStream mInputStream;
    private Uri mUri;
    private long mBytesRemaining;
    private boolean mOpened;
    private Cipher mCipher;
    private SecretKey mSecretKey;
    private IvParameterSpec mIvParameterSpec;

    public CryptoFileDataSource(DecryptingKeys decryptingKeys) {
        mCipher = decryptingKeys.getCipher();
        mSecretKey = decryptingKeys.getSecretKey();
        mIvParameterSpec = decryptingKeys.getIvParameterSpec();
    }

    @Override
    public long open(DataSpec dataSpec) throws EncryptedFileDataSourceException {
        // if we're open, we shouldn't need to open again, fast-fail
        if (mOpened) {
            return mBytesRemaining;
        }
        // #getUri is part of the contract...
        mUri = dataSpec.uri;
        // put all our throwable work in a single block, wrap the error in a custom Exception
        try {
            setupInputStream();
            skipToPosition(dataSpec);
            computeBytesRemaining(dataSpec);
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        }
        // if we made it this far, we're open
        mOpened = true;

        return mBytesRemaining;
    }

    private void setupInputStream() throws IOException {
        final File encryptedFile = new File(mUri.getPath());
        final InitVectorFileInputStream fileInputStream = new InitVectorFileInputStream(encryptedFile);
        mInputStream = new StreamingCipherInputStream(fileInputStream, mCipher, mSecretKey, mIvParameterSpec);
    }

    private void skipToPosition(DataSpec dataSpec) throws IOException {
        mInputStream.forceSkip(dataSpec.position);
    }

    private void computeBytesRemaining(DataSpec dataSpec) throws IOException {
        if (dataSpec.length != C.LENGTH_UNSET) {
            mBytesRemaining = dataSpec.length;
        } else {
            mBytesRemaining = mInputStream.available();
            if (mBytesRemaining == Integer.MAX_VALUE) {
                mBytesRemaining = C.LENGTH_UNSET;
            }
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws EncryptedFileDataSourceException {
        // fast-fail if there's 0 quantity requested or we think we've already processed everything
        if (readLength == 0) {
            return 0;
        } else if (mBytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        }
        // constrain the read length and try to read from the cipher input stream
        int bytesToRead = getBytesToRead(readLength);
        int bytesRead;
        try {
            bytesRead = mInputStream.read(buffer, offset, bytesToRead);
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        }
        // if we get a -1 that means we failed to read - we're either going to EOF error or broadcast EOF
        if (bytesRead == -1) {
            if (mBytesRemaining != C.LENGTH_UNSET) {
                throw new EncryptedFileDataSourceException(new EOFException());
            }
            return C.RESULT_END_OF_INPUT;
        }
        // we can't decrement bytes remaining if it's just a flag representation (as opposed to a mutable numeric quantity)
        if (mBytesRemaining != C.LENGTH_UNSET) {
            mBytesRemaining -= bytesRead;
        }

        return bytesRead;
    }

    private int getBytesToRead(int bytesToRead) {
        if (mBytesRemaining == C.LENGTH_UNSET) {
            return bytesToRead;
        }
        return (int) Math.min(mBytesRemaining, bytesToRead);
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public void close() throws EncryptedFileDataSourceException {
        mUri = null;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            throw new EncryptedFileDataSourceException(e);
        } finally {
            mInputStream = null;
            if (mOpened) {
                mOpened = false;
            }

        }
    }

    public static final class EncryptedFileDataSourceException extends IOException {
        public EncryptedFileDataSourceException(IOException cause) {
            super(cause);
        }
    }

}
