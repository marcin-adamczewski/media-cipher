package com.appunite.mediaenryption.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediaenryption.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String SAMPLE_MP3_URL = "http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2011.mp3";
    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/encodedMp3File";

    private String mp3DownloadUrl;
    private BaseDownloadTask.FinishListener downloadFinishedListener;
    private SimpleExoPlayer exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp3DownloadUrl = getIntent().getStringExtra(EXTRA_URL);
        mp3DownloadUrl = mp3DownloadUrl == null ? SAMPLE_MP3_URL : mp3DownloadUrl;

        exoPlayer = ExoHelper.simpleInstance(this);

        downloadFinishedListener = new BaseDownloadTask.FinishListener() {
            @Override
            public void over(final BaseDownloadTask task) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Encrypted Mp3 download completed", Toast.LENGTH_SHORT).show();
                        assertThatDownloadedFileIsNotAudioFile();
                        preparePlayerAndPlay(DOWNLOAD_PATH);
                    }
                });
            }
        };

        findViewById(R.id.init_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadFile(mp3DownloadUrl);
            }
        });

        findViewById(R.id.play_pause_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
            }
        });

        ((SeekBar) findViewById(R.id.seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                exoPlayer.seekTo((long) (exoPlayer.getDuration() * (float) progress / 100f));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
    }

    private void assertThatDownloadedFileIsNotAudioFile() {
        try {
            Checker.checkArgument(new File(DOWNLOAD_PATH).exists());
            final MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(DOWNLOAD_PATH);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Media extractor cannot read the file - that means it is correctly encoded !", Toast.LENGTH_LONG).show();
        }
    }

    private void downloadFile(final String url) {
        if (!askStoragePermissionsIfNeeded()) {
            return;
        }

        final File file = new File(DOWNLOAD_PATH);
        if (file.exists()) {
            file.delete();
        }

        final FileDownloadListener fileDownloadListener = new FileDownloadSampleListener() {};

        final FileDownloader fileDownloader = FileDownloader.getImpl();
        fileDownloader
                .create(url)
                .setPath(DOWNLOAD_PATH)
                .addFinishListener(downloadFinishedListener)
                .setListener(fileDownloadListener)
                .setAutoRetryTimes(1)
                .asInQueueTask()
                .enqueue();
        fileDownloader.start(fileDownloadListener, false);
    }

    private void preparePlayerAndPlay(final String url) {
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.prepare(ExoHelper.getAudioSource(Uri.parse(url), this));
    }

    private boolean askStoragePermissionsIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == 2 && arePermissionsGranted(grantResults)) {
            downloadFile(mp3DownloadUrl);
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
