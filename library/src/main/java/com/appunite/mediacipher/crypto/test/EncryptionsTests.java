package com.appunite.mediacipher.crypto.test;


import android.os.Environment;


import com.appunite.mediacipher.helpers.Logger;
import com.appunite.mediacipher.crypto.AESCrypter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

class EncryptionsTests {

/*    private static final String TAG = "EncryptionsTests";

    public static void testRandomStringEncryptDecrypt(final AESCrypter aesCrypter) {
        try {
            Logger.logDebug(TAG, "starting test testRandomStringEncryptDecrypt");

            final String data = "text" + String.valueOf(new Random().nextInt());
            final byte[] dataBytes = data.getBytes("UTF-8");
            Logger.logDebug(TAG, "data to encrypt: " + data);

            // encode
            ByteArrayOutputStream encodedOutput = new ByteArrayOutputStream();
            final CipherOutputStream encryptStream = aesCrypter.getEncryptingStream(encodedOutput);

            encryptStream.write(dataBytes);
            encryptStream.flush();
            encryptStream.close();

            final byte[] encodedBytes = encodedOutput.toByteArray();
            final boolean equals = Arrays.equals(dataBytes, encodedBytes);
            if (equals) {
                Logger.logError(TAG, "decoded and encoded bytes are equal ! Wrong encryption");
            }

            // decode
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encodedBytes);
            final CipherInputStream decryptStream = aesCrypter.getDecryptingStream(byteArrayInputStream);

            ByteArrayOutputStream decodedOutput = new ByteArrayOutputStream();
            final long copiedBytes = copyStreams(decryptStream, decodedOutput);
            Logger.logDebug(TAG, "copied bytes: " + copiedBytes);

            final byte[] bytes = decodedOutput.toByteArray();
            final String decoded = new String(bytes, "UTF-8");
            Logger.logDebug(TAG, "decrypted data: " + decoded);

            decryptStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError(TAG, "error: " + e.getMessage());
        }
    }

    public static void testMusicDecryption(AESCrypter aesCrypter, File encryptedFile) {
        Logger.logDebug(TAG, "starting test testMusicDecryption");
        try {
            final String musicDirPath = encryptedFile.getParentFile().getPath();
            final CipherInputStream decryptStream = aesCrypter.getDecryptingStream(new FileInputStream(encryptedFile));
            Logger.logDebug(TAG, "available bytes to copy from file: " + encryptedFile.length());
            Logger.logDebug(TAG, "available bytes to copy from stream: " + decryptStream.available());

            final String decodedFilePath = musicDirPath + "/decryptedFile";
            final File decodedFile = new File(decodedFilePath);

            final FileOutputStream fileOutputStream = new FileOutputStream(decodedFile);
            final long copiedBytes = copyStreams(decryptStream, fileOutputStream);

            decryptStream.close();
            fileOutputStream.close();
            Logger.logDebug(TAG, "copied bytes: " + copiedBytes);
        } catch (Exception e) {
            Logger.logError(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testFileEncryptionDecryption(AESCrypter aesCrypter) {
        Logger.logDebug(TAG, "starting test testFileEncryptionDecryption");
        try {
            // encrypt fileWithEncryptedText with text
            File fileWithEncryptedText = new File(Environment.getExternalStorageDirectory().getPath() + "/fileToEncrypt");
            final String text = "text" + String.valueOf(new Random().nextInt());
            final byte[] dataBytes = text.getBytes("UTF-8");
            FileOutputStream fileOutputStream = new FileOutputStream(fileWithEncryptedText);
            final CipherOutputStream encryptStream = aesCrypter.getEncryptingStream(fileOutputStream);
            encryptStream.write(dataBytes);
            encryptStream.flush();
            encryptStream.close();

            // decrypt fileWithEncryptedText
            final CipherInputStream decryptStream = aesCrypter.getDecryptingStream(new FileInputStream(fileWithEncryptedText));
            Logger.logDebug(TAG, "available bytes to copy from file: " + fileWithEncryptedText.length());
            Logger.logDebug(TAG, "available bytes to copy from stream: " + decryptStream.available());

            File fileWithDecryptedText = new File(Environment.getExternalStorageDirectory().getPath() + "/decryptedFile");
            final FileOutputStream decryptedOS = new FileOutputStream(fileWithDecryptedText);
            final long copiedBytes = copyStreams(decryptStream, decryptedOS);
            Logger.logDebug(TAG, "copied bytes: " + copiedBytes);


            ByteArrayOutputStream readDecryptedBytes = new ByteArrayOutputStream();
            copyStreams(new FileInputStream(fileWithDecryptedText), readDecryptedBytes);

            decryptStream.close();
            fileOutputStream.close();

            final String decryptedText = new String(readDecryptedBytes.toByteArray(), "UTF-8");
            Logger.logDebug(TAG, "text before encryption: " + text + " text after decryption: " + decryptedText);

            fileWithDecryptedText.delete();
            fileWithDecryptedText.delete();
        } catch (Exception e) {
            Logger.logError(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testEcryptFile(AESCrypter aesCrypter) {
        Logger.logDebug(TAG, "starting test testEcryptFile");
        try {
            // encrypt fileToEncrypt with text
            File fileToEncrypt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/24h zumba 225.mp3");
            final CipherOutputStream encryptStream = aesCrypter.getEncryptingStream(new FileOutputStream(new File(fileToEncrypt.getParentFile().getPath() + "/24encrypted")));
            Logger.logDebug(TAG, "file length in bytes before encryption: " + fileToEncrypt.length());

            final long copiedBytes = copyStreams(new FileInputStream(fileToEncrypt), encryptStream);
            encryptStream.flush();
            encryptStream.close();

            Logger.logDebug(TAG, "copied bytes after encryption: " + copiedBytes);

        } catch (Exception e) {
            Logger.logError(TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static long copyStreams(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buf = new byte[8192];
        long total = 0;

        while (true) {
            int read = inStream.read(buf);
            if (read == -1) {
                break;
            }
            outStream.write(buf, 0, read);
            total += read;
        }
        
        return total;
    }*/
}
