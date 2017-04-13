package com.appunite.mediacipher.crypto.exoplayer;


import com.appunite.mediacipher.crypto.DecryptingKeys;
import com.appunite.mediacipher.crypto.IncreasedBufferCipherInputStream;
import com.appunite.mediacipher.crypto.InitVectorFileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class StreamingCipherInputStream extends IncreasedBufferCipherInputStream {

    private static final int AES_BLOCK_SIZE = 16;

    private InputStream upstream;
    private Cipher cipher;
    private SecretKey secretKey;
    private IvParameterSpec ivParameterSpec;

    public StreamingCipherInputStream(final InitVectorFileInputStream inputStream,
                                      final DecryptingKeys decryptingKeys) {
        super(inputStream, decryptingKeys.getCipher());
        upstream = inputStream;
        this.cipher = decryptingKeys.getCipher();
        this.secretKey = decryptingKeys.getSecretKey();
        this.ivParameterSpec = decryptingKeys.getIvParameterSpec();
    }

    public long forceSkip(long bytesToSkip) throws IOException {
        long skipped = upstream.skip(bytesToSkip);
        try {
            int skip = (int) (bytesToSkip % AES_BLOCK_SIZE);
            long blockOffset = bytesToSkip - skip;
            long numberOfBlocks = blockOffset / AES_BLOCK_SIZE;

            final BigInteger ivForOffsetAsBigInteger = new BigInteger(1, ivParameterSpec.getIV()).add(BigInteger.valueOf(numberOfBlocks));
            byte[] ivForOffsetByteArray = ivForOffsetAsBigInteger.toByteArray();
            IvParameterSpec computedIvParameterSpecForOffset;
            if (ivForOffsetByteArray.length < AES_BLOCK_SIZE) {
                byte[] resizedIvForOffsetByteArray = new byte[AES_BLOCK_SIZE];
                System.arraycopy(ivForOffsetByteArray, 0, resizedIvForOffsetByteArray, AES_BLOCK_SIZE - ivForOffsetByteArray.length, ivForOffsetByteArray.length);
                computedIvParameterSpecForOffset = new IvParameterSpec(resizedIvForOffsetByteArray);
            } else {
                computedIvParameterSpecForOffset = new IvParameterSpec(ivForOffsetByteArray, ivForOffsetByteArray.length - AES_BLOCK_SIZE, AES_BLOCK_SIZE);
            }
            cipher.init(Cipher.DECRYPT_MODE, secretKey, computedIvParameterSpecForOffset);
            byte[] skipBuffer = new byte[skip];

            cipher.update(skipBuffer, 0, skip, skipBuffer);
            Arrays.fill(skipBuffer, (byte) 0);
        } catch (Exception e) {
            return 0;
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return upstream.available();
    }

}
