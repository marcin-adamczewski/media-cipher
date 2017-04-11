package com.appunite.mediaenryption.sample;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.webkit.URLUtil;

import com.appunite.mediacipher.MediaCipher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import javax.annotation.Nonnull;

public class ExoHelper {

    @Nonnull
    public static SimpleExoPlayer simpleInstance(@Nonnull final Context context) {
        return ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(), new DefaultLoadControl());
    }

    @Nonnull
    public static MediaSource getAudioSource(@Nonnull final Uri uri,
                                             @Nonnull final Context context) {
        final DataSource.Factory factory;

        if (!URLUtil.isNetworkUrl(uri.toString())) {
            factory = MediaCipher.getInstance().getEncryptedFileDataSourceFactory();
        } else {
            factory = new DefaultDataSourceFactory(context, context.getPackageName());
        }

        return new ExtractorMediaSource(uri, factory, new DefaultExtractorsFactory(), new Handler(), null);
    }
}
