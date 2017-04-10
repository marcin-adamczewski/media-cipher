package com.appunite.mediaenryption.crypto;


import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.appunite.mediaenryption.KeysPreferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class AESCrypterBelowM extends AESCrypter {

    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String AES_MODE = "AES/ECB/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final String PROVIDER_ANDROID_OPEN_SSL = "AndroidOpenSSL";
    private static final String PROVIDER_BC = "BC";

    public AESCrypterBelowM(@NonNull final Context context, @NonNull final KeysPreferences keysPreferences) {
        super(context, keysPreferences);
    }

    @NonNull
    @Override
    protected CipherOutputStream getCipherOutputStream(@NonNull final OutputStream outputStream,
                                                       @NonNull final SecretKey secretKey) throws Exception {
        final Cipher encryptCipher = Cipher.getInstance(AES_MODE, PROVIDER_BC);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return new CipherOutputStream(outputStream, encryptCipher);
    }

    @NonNull
    @Override
    protected CipherInputStream getCipherInputStream(@NonNull final InputStream inputStream, @NonNull final SecretKey secretKey) throws Exception {
        final Cipher decryptCipher = Cipher.getInstance(AES_MODE, PROVIDER_BC);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new AvailableCipherInputStream(inputStream, decryptCipher);
    }

    @NonNull
    @Override
    protected SecretKey getAESKey(@NonNull final String keyAlias) throws Exception {
        final String enryptedKeyB64 = keysPreferences.getEncryptedAESKey();
        final byte[] decodeKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
        final byte[] decryptedAES = rsaDecrypt(decodeKey, keyAlias);

        return new SecretKeySpec(decryptedAES, AES_ALGORITHM);
    }

    @NonNull
    @Override
    protected SecretKey generateNewAESKey(@NonNull final String rsaKeyAlias) throws Exception {
        generateRSAKey(rsaKeyAlias);

        byte[] aesKey = new byte[16];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(aesKey);

        byte[] encryptedKey = rsaEncrypt(aesKey, rsaKeyAlias);
        final String enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);

        keysPreferences.edit().setEncryptedAESKey(enryptedKeyB64);

        return new SecretKeySpec(aesKey, AES_ALGORITHM);
    }

    private void generateRSAKey(@NonNull final String keyAlias) throws Exception {
        final Calendar start = Calendar.getInstance();
        final Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);
        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(keyAlias)
                .setSubject(new X500Principal("CN=" + keyAlias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM, ANDROID_KEY_STORE);
        kpg.initialize(spec);
        kpg.generateKeyPair();
    }

    private byte[] rsaEncrypt(byte[] secret, @NonNull String keyAlias) throws Exception {
        final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);

        final Cipher inputCipher = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_OPEN_SSL);
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted, @NonNull String keyAlias) throws Exception {
        final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);

        final Cipher output = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_OPEN_SSL);
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

        final CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);
        List<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return bytes;
    }
}
