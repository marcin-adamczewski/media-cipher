package com.appunite.mediacipher.crypto;


import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.appunite.mediacipher.KeysPreferences;
import com.appunite.mediacipher.helpers.Checker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class AESCrypterBelowM extends AESCrypter {

    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final String PROVIDER_ANDROID_OPEN_SSL = "AndroidOpenSSL";

    public AESCrypterBelowM(@Nonnull final Context context, @Nonnull final KeysPreferences keysPreferences) {
        super(context, keysPreferences);
    }

    @Nonnull
    @Override
    protected SecretKey getAESKey(@Nonnull final String keyAlias) throws Exception {
        final String enryptedKeyB64 = keysPreferences.getEncryptedAESKey();
        final byte[] decodeKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
        final byte[] decryptedAES = rsaDecrypt(decodeKey, keyAlias);

        return new SecretKeySpec(decryptedAES, AES_ALGORITHM);
    }

    @Nonnull
    @Override
    protected SecretKey generateNewAESKey(@Nonnull final String rsaKeyAlias) throws Exception {
        generateRSAKey(rsaKeyAlias);

        byte[] aesKey = new byte[16];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(aesKey);

        byte[] encryptedKey = rsaEncrypt(aesKey, rsaKeyAlias);
        final String enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);

        keysPreferences.setEncryptedAESKey(enryptedKeyB64);

        return new SecretKeySpec(aesKey, AES_ALGORITHM);
    }

    private void generateRSAKey(@Nonnull final String keyAlias) throws Exception {
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

    private byte[] rsaEncrypt(byte[] secret, @Nonnull String keyAlias) throws Exception {
        final KeyStore.PrivateKeyEntry privateKeyEntry = getKeystoreEntry(keyAlias);

        final Cipher rsaEncryptCipher = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_OPEN_SSL);
        rsaEncryptCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, rsaEncryptCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encryptedAes, @Nonnull String keyAlias) throws Exception {
        final KeyStore.PrivateKeyEntry privateKeyEntry = getKeystoreEntry(keyAlias);

        final Cipher rsaDecryptCipher = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_OPEN_SSL);
        rsaDecryptCipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

        final CipherInputStream inStream = new CipherInputStream(new ByteArrayInputStream(encryptedAes), rsaDecryptCipher);
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        byte[] buf = new byte[16];
        while (true) {
            int read = inStream.read(buf);
            if (read == -1) {
                break;
            }
            outStream.write(buf, 0, read);
        }

        return outStream.toByteArray();
    }

    @Nonnull
    private KeyStore.PrivateKeyEntry getKeystoreEntry(@Nonnull String keyAlias) throws Exception {
        final KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, null);
        Checker.checkNotNull(keyEntry, "Key entry is null for keyAlias: " + keyAlias);

        return keyEntry;
    }
}
