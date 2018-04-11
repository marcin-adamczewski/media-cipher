package com.appunite.mediaenryption.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediaenryption.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    //private static final String SAMPLE_MP3_URL = "http://www.noiseaddicts.com/samples_1w72b820/4250.mp3"; // 0.6 MB
    //private static final String SAMPLE_MP3_URL = "http://www.noiseaddicts.com/samples_1w72b820/4208.mp3"; // 1.3 MB
    //private static final String SAMPLE_MP3_URL = "http://www.noiseaddicts.com/samples_1w72b820/4246.mp3"; // 0.6 MB
    private static final String SAMPLE_MP3_URL = "http://www.noiseaddicts.com/samples_1w72b820/4352.mp3"; // 3 MB
    //private static final String SAMPLE_MP4_URL = "http://mirrors.standaloneinstaller.com/video-sample/dolbycanyon.mp4"; 3.01 MB
    private static final String SAMPLE_MP4_URL = "http://mirrors.standaloneinstaller.com/video-sample/jellyfish-25-mbps-hd-hevc.mp4"; // 31.3 MB
    //private static final String SAMPLE_MP4_URL = "http://mirrors.standaloneinstaller.com/video-sample/small.mp4"; // 0.17 MB

    private static final String DOWNLOAD_PATH = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/encodedFile";

    private String mediaDownloadUrl;
    private SimpleExoPlayer exoPlayer;
    private TextView downloadingProgressTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    private void downloadFile(final String url) {
        if (!checkStoragePermissions()) {
            return;
        }

        final File file = new File(DOWNLOAD_PATH);
        if (file.exists()) {
            file.delete();
        }

        final DownloadListener listener = new MyDownloadListener() {

            @Override
            public void progress(@NonNull final DownloadTask task, final long currentOffset, final long totalLength) {
                final int progress = (int) (100 * (float) currentOffset / (float) totalLength);
                downloadingProgressTv.setText("Downloading progress: " + progress + " %");
            }

            @Override
            public void taskEnd(@NonNull final DownloadTask task,
                                @NonNull final EndCause cause,
                                @Nullable final Exception realCause,
                                @NonNull final Listener1Assist.Listener1Model model) {
                if (cause == EndCause.COMPLETED) {
                    Toast.makeText(MainActivity.this, "Media downloaded and encrypted", Toast.LENGTH_SHORT).show();
                    assertThatDownloadedFileIsNotAudioFile();
                    preparePlayerAndPlay(DOWNLOAD_PATH);
                    downloadingProgressTv.setText(null);
                } else {
                    Log.e("lol", "cause: " + cause + " error: " + realCause);
                }
            }

            @Override
            protected void error(@NonNull final DownloadTask task, @NonNull final Exception e) {
                Toast.makeText(MainActivity.this, "Cannot download the file with error: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        final DownloadTask task = new DownloadTask.Builder(url, file)
                .setMinIntervalMillisCallbackProcess(300)
                .setPassIfAlreadyCompleted(false)
                .build();

        task.cancel();

        task.enqueue(listener);
    }

    private void preparePlayerAndPlay(final String url) {
        try {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.prepare(ExoHelper.getAudioSource(Uri.parse(url), this));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot load media with error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
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

    private void assertThatDownloadedFileIsNotAudioFile() {
        try {
            Checker.checkArgument(new File(DOWNLOAD_PATH).exists(), "No such file in path: " + DOWNLOAD_PATH);
            final MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(DOWNLOAD_PATH);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Media encryption test succeed", Toast.LENGTH_LONG).show();
        }
    }
}
