package com.appunite.mediacipher.crypto.download;


/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.system.Os;

import com.appunite.mediacipher.Listener;
import com.appunite.mediacipher.crypto.AESCrypter;
import com.appunite.mediacipher.helpers.Logger;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.file.DownloadOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;
import javax.crypto.CipherOutputStream;

public class DownloadEncryptingOutputStream implements DownloadOutputStream {

    @NonNull private final FileChannel channel;
    @NonNull private final ParcelFileDescriptor pdf;
    @NonNull private final CipherOutputStream out;

    public DownloadEncryptingOutputStream(Context context,
                                          AESCrypter aesCrypter,
                                          File file,
                                          int bufferSize) throws Exception {
        final ParcelFileDescriptor pdf = context.getContentResolver().openFileDescriptor(
                Uri.fromFile(file), "rw");
        if (pdf == null)
            throw new FileNotFoundException("result of " + file.getAbsolutePath() + " is null!");
        this.pdf = pdf;

        final FileOutputStream fos = new FileOutputStream(pdf.getFileDescriptor());
        channel = fos.getChannel();
        out = aesCrypter.getEncryptingStream(new BufferedOutputStream(fos, bufferSize));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flushAndSync() throws IOException {
        out.flush();
        pdf.getFileDescriptor().sync();
    }

    @Override
    public void seek(long offset) throws IOException {
        channel.position(offset);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        final String tag = "DownloadUriOutputStream";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Os.ftruncate(pdf.getFileDescriptor(), newLength);
            } catch (Throwable e) {
                Util.w(tag, "It can't pre-allocate length(" + newLength + ") on the sdk"
                        + " version(" + Build.VERSION.SDK_INT + "), because of " + e);
            }
        } else {
            Util.w(tag,
                    "It can't pre-allocate length(" + newLength + ") on the sdk "
                            + "version(" + Build.VERSION.SDK_INT + ")");
        }
    }

    public static class Factory implements DownloadOutputStream.Factory {

        @Nonnull private final AESCrypter aesCrypter;
        @Nonnull private final Listener listener;

        public Factory(@Nonnull AESCrypter aesCrypter,
                       @Nonnull Listener listener) {
            this.aesCrypter = aesCrypter;
            this.listener = listener;
        }

        @Override
        public DownloadOutputStream create(final Context context,
                                           final File file,
                                           final int flushBufferSize) throws FileNotFoundException {
            try {
                return new DownloadEncryptingOutputStream(context, aesCrypter, file, flushBufferSize);
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) {
                    throw (FileNotFoundException) e;
                } else {
                    listener.onError(e);
                    Logger.logError("Cannot create DownloadOutputStream with error: " + e.getMessage());
                    return null;
                }
            }
        }

        @Override
        public DownloadOutputStream create(final Context context,
                                           final Uri uri,
                                           final int flushBufferSize) throws FileNotFoundException {
            return create(context, new File(uri.getPath()), flushBufferSize);
        }

        @Override
        public boolean supportSeek() {
            return false;
        }
    }
}

