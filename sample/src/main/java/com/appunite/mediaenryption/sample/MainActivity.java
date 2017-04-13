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

import com.appunite.mediacipher.helpers.Checker;
import com.appunite.mediaenryption.R;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String SAMPLE_MP3_URL = "http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2011.mp3";
    private static final String SAMPLE_MP4_URL = "https://dwknz3zfy9iu1.cloudfront.net/uscenes_h-264_hd_test.mp4";

    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/encodedMp3File";

    private String mediaDownloadUrl;
    private BaseDownloadTask.FinishListener downloadFinishedListener;
    private SimpleExoPlayer exoPlayer;
    private TextView downloadingProgressTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaDownloadUrl = getIntent().getStringExtra(EXTRA_URL);
        mediaDownloadUrl = mediaDownloadUrl == null ? SAMPLE_MP3_URL : mediaDownloadUrl;

        downloadingProgressTv = (TextView) findViewById(R.id.downloading_progress_tv);

        final SimpleExoPlayerView exoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayerview);
        exoPlayer = ExoHelper.simpleInstance(this);
        exoPlayerView.setPlayer(exoPlayer);

        ((RadioGroup) findViewById(R.id.radio_group))
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final RadioGroup group, @IdRes final int checkedId) {
                        mediaDownloadUrl = checkedId == R.id.radio_mp3 ? SAMPLE_MP3_URL : SAMPLE_MP4_URL;
                    }
                });

        downloadFinishedListener = new BaseDownloadTask.FinishListener() {
            @Override
            public void over(final BaseDownloadTask task) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Media downloaded and encrypted", Toast.LENGTH_SHORT).show();
                        assertThatDownloadedFileIsNotAudioFile();
                        preparePlayerAndPlay(DOWNLOAD_PATH);
                        downloadingProgressTv.setText(null);
                    }
                });
            }
        };

        findViewById(R.id.init_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                downloadFile(mediaDownloadUrl);
            }
        });
    }

    private void assertThatDownloadedFileIsNotAudioFile() {
        try {
            Checker.checkArgument(new File(DOWNLOAD_PATH).exists());
            final MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(DOWNLOAD_PATH);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Media encryption test succeed", Toast.LENGTH_LONG).show();
        }
    }

    private void downloadFile(final String url) {
        if (!checkStoragePermissions()) {
            return;
        }

        final File file = new File(DOWNLOAD_PATH);
        if (file.exists()) {
            file.delete();
        }

        final FileDownloadListener fileDownloadListener = new FileDownloadSampleListener() {
            @Override
            protected void progress(final BaseDownloadTask task, final int soFarBytes, final int totalBytes) {
                super.progress(task, soFarBytes, totalBytes);
                final int progress = (int) (100 * (float) soFarBytes / (float) totalBytes);
                downloadingProgressTv.setText("Downloading progress: " + progress + " %");
            }
        };

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
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
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
