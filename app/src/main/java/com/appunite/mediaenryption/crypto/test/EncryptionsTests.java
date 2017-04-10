package com.appunite.mediaenryption.crypto.test;


import android.os.Environment;


import com.appunite.mediaenryption.LogHelper;
import com.appunite.mediaenryption.crypto.AESCrypter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class EncryptionsTests {

/*    private static final String TAG = "EncryptionsTests";

    public static void testRandomStringEncryptDecrypt(final AESCrypter aesCrypter) {
        try {
            LogHelper.logIfDebug(TAG, "starting test testRandomStringEncryptDecrypt");

            final String data = "text" + String.valueOf(new Random().nextInt());
            final byte[] dataBytes = data.getBytes("UTF-8");
            LogHelper.logIfDebug(TAG, "data to encrypt: " + data);

            // encode
            ByteArrayOutputStream encodedOutput = new ByteArrayOutputStream();
            final CipherOutputStream encryptStream = aesCrypter.getEncryptStream(encodedOutput);

            encryptStream.write(dataBytes);
            encryptStream.flush();
            encryptStream.close();

            final byte[] encodedBytes = encodedOutput.toByteArray();
            final boolean equals = Arrays.equals(dataBytes, encodedBytes);
            if (equals) {
                LogHelper.logIfDebug(TAG, "decoded and encoded bytes are equal ! Wrong encryption");
            }

            // decode
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encodedBytes);
            final CipherInputStream decryptStream = aesCrypter.getDecryptStream(byteArrayInputStream);

            ByteArrayOutputStream decodedOutput = new ByteArrayOutputStream();
            final long copiedBytes = ByteStreams.copy(decryptStream, decodedOutput);
            LogHelper.logIfDebug(TAG, "copied bytes: " + copiedBytes);

            final byte[] bytes = decodedOutput.toByteArray();
            final String decoded = new String(bytes, "UTF-8");
            LogHelper.logIfDebug(TAG, "decrypted data: " + decoded);

            decryptStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.logIfDebug(TAG, "error: " + e.getMessage());
        }
    }

    public static void testMusicDecryption(AESCrypter aesCrypter, File encryptedFile) {
        LogHelper.logIfDebug(TAG, "starting test testMusicDecryption");
        try {
            final String musicDirPath = encryptedFile.getParentFile().getPath();
            final CipherInputStream decryptStream = aesCrypter.getDecryptStream(new FileInputStream(encryptedFile));
            LogHelper.logIfDebug(TAG, "available bytes to copy from file: " + encryptedFile.length());
            LogHelper.logIfDebug(TAG, "available bytes to copy from stream: " + decryptStream.available());

            final String decodedFilePath = musicDirPath + "/decryptedFile";
            final File decodedFile = new File(decodedFilePath);

            final FileOutputStream fileOutputStream = new FileOutputStream(decodedFile);
            final long copiedBytes = ByteStreams.copy(decryptStream, fileOutputStream);

            decryptStream.close();
            fileOutputStream.close();
            LogHelper.logIfDebug("lol", "copied bytes: " + copiedBytes);
        } catch (Exception e) {
            LogHelper.logIfDebug("lol", "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testFileEncryptionDecryption(AESCrypter aesCrypter) {
        LogHelper.logIfDebug(TAG, "starting test testFileEncryptionDecryption");
        try {
            // encrypt fileWithEncryptedText with text
            File fileWithEncryptedText = new File(Environment.getExternalStorageDirectory().getPath() + "/fileToEncrypt");
            final String text = "text" + String.valueOf(new Random().nextInt());
            final byte[] dataBytes = text.getBytes("UTF-8");
            FileOutputStream fileOutputStream = new FileOutputStream(fileWithEncryptedText);
            final CipherOutputStream encryptStream = aesCrypter.getEncryptStream(fileOutputStream);
            encryptStream.write(dataBytes);
            encryptStream.flush();
            encryptStream.close();

            // decrypt fileWithEncryptedText
            final CipherInputStream decryptStream = aesCrypter.getDecryptStream(new FileInputStream(fileWithEncryptedText));
            LogHelper.logIfDebug(TAG, "available bytes to copy from file: " + fileWithEncryptedText.length());
            LogHelper.logIfDebug(TAG, "available bytes to copy from stream: " + decryptStream.available());

            File fileWithDecryptedText = new File(Environment.getExternalStorageDirectory().getPath() + "/decryptedFile");
            final FileOutputStream decryptedOS = new FileOutputStream(fileWithDecryptedText);
            final long copiedBytes = ByteStreams.copy(decryptStream, decryptedOS);
            LogHelper.logIfDebug(TAG, "copied bytes: " + copiedBytes);


            ByteArrayOutputStream readDecryptedBytes = new ByteArrayOutputStream();
            ByteStreams.copy(new FileInputStream(fileWithDecryptedText), readDecryptedBytes);

            decryptStream.close();
            fileOutputStream.close();

            final String decryptedText = new String(readDecryptedBytes.toByteArray(), "UTF-8");
            LogHelper.logIfDebug(TAG, "text before encryption: " + text + " text after decryption: " + decryptedText);

            fileWithDecryptedText.delete();
            fileWithDecryptedText.delete();
        } catch (Exception e) {
            LogHelper.logIfDebug(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testEcryptFile(AESCrypter aesCrypter) {
        LogHelper.logIfDebug(TAG, "starting test testEcryptFile");
        try {
            // encrypt fileToEncrypt with text
            File fileToEncrypt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/24h zumba 225.mp3");
            final CipherOutputStream encryptStream = aesCrypter.getEncryptStream(new FileOutputStream(new File(fileToEncrypt.getParentFile().getPath() + "/24encrypted")));
            LogHelper.logIfDebug(TAG, "file length in bytes before encryption: " + fileToEncrypt.length());

            final long copiedBytes = ByteStreams.copy(new FileInputStream(fileToEncrypt), encryptStream);
            encryptStream.flush();
            encryptStream.close();

            LogHelper.logIfDebug(TAG, "copied bytes after encryption: " + copiedBytes);

        } catch (Exception e) {
            LogHelper.logIfDebug(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }*/
}
