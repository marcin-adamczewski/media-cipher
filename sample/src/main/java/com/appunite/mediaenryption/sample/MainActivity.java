package com.appunite.mediaenryption.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.mediacipher.MediaCipher;
import com.appunite.mediacipher.crypto.download.EncryptingDownloader;
import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediaenryption.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Func;
import com.tonyodev.fetch2.Request;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String SAMPLE_MP3_URL = "http://www.noiseaddicts.com/samples_1w72b820/4250.mp3";
    private static final String SAMPLE_MP4_URL = "https://dwknz3zfy9iu1.cloudfront.net/uscenes_h-264_hd_test.mp4";

    private static final String DOWNLOAD_PATH = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/encodedMp3File";

    private static final boolean SHOW_LOGS = true;

    private String mediaDownloadUrl;
    private SimpleExoPlayer exoPlayer;
    private TextView downloadingProgressTv;
    private Fetch fetch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient okHttp = new OkHttpClient.Builder()
                .build();

        fetch = new Fetch.Builder(this, "EncryptingFetch")
                .setDownloadConcurrentLimit(3)
                .enableLogging(SHOW_LOGS)
                .setProgressReportingInterval(300)
                .enableRetryOnNetworkGain(true)
                .setDownloader(MediaCipher.getInstance().getEncryptingDownloader(okHttp)) // most important part
                .build();

        mediaDownloadUrl = getIntent().getStringExtra(EXTRA_URL);
        mediaDownloadUrl = mediaDownloadUrl == null ? SAMPLE_MP3_URL : mediaDownloadUrl;

        downloadingProgressTv = findViewById(R.id.downloading_progress_tv);

        final SimpleExoPlayerView exoPlayerView = findViewById(R.id.exoplayerview);
        exoPlayer = ExoHelper.simpleInstance(this);
        exoPlayerView.setPlayer(exoPlayer);

        ((RadioGroup) findViewById(R.id.radio_group))
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final RadioGroup group, @IdRes final int checkedId) {
                        mediaDownloadUrl = checkedId == R.id.radio_mp3 ? SAMPLE_MP3_URL : SAMPLE_MP4_URL;
                    }
                });

        findViewById(R.id.init_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadFile(mediaDownloadUrl);
            }
        });

        final FetchListener fileDownloadListener = new AbstractFetchListener() {
            @Override
            public void onProgress(Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond);
                final int progress = (int) (100 * (float) download.getDownloaded() / (float) download.getTotal());
                downloadingProgressTv.setText("Downloading progress: " + progress + " %");
            }

            @Override
            public void onCompleted(Download download) {
                super.onCompleted(download);
                assertThatDownloadedFileIsNotMediaFile();
                Toast.makeText(MainActivity.this, "Media downloaded and encrypted",
                        Toast.LENGTH_SHORT).show();
                preparePlayerAndPlay(DOWNLOAD_PATH);
                downloadingProgressTv.setText(null);
            }

            @Override
            public void onError(Download download) {
                super.onError(download);
                Toast.makeText(MainActivity.this, "Cannot download the file with error: "
                        + download.getError().getThrowable(), Toast.LENGTH_LONG).show();
            }
        };
        fetch.addListener(fileDownloadListener);

    }

    private void downloadFile(final String url) {
        if (!checkStoragePermissions()) {
            return;
        }

        final Request request = new Request(url.hashCode(), url, DOWNLOAD_PATH);
        fetch.delete(request.getId());
        fetch.enqueue(request, null, null);
    }

    private void preparePlayerAndPlay(final String url) {
        try {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.prepare(ExoHelper.getAudioSource(Uri.parse(url), this));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot load media with error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void assertThatDownloadedFileIsNotMediaFile() {
        try {
            Checker.checkArgument(new File(DOWNLOAD_PATH).exists(), "No such file in path: " + DOWNLOAD_PATH);
            final MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(DOWNLOAD_PATH);
        } catch (IOException e) {
            Toast.makeText(this, "Media encryption test succeed", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == 2 && arePermissionsGranted(grantResults)) {
            downloadFile(mediaDownloadUrl);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static boolean arePermissionsGranted(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return grantResults.length > 0;
    }
}
