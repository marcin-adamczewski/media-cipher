package com.appunite.mediaenryption.sample;


import android.support.annotation.NonNull;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener3;

public abstract class MyDownloadListener extends DownloadListener3 {

    @Override
    protected void started(@NonNull final DownloadTask task) {

    }

    @Override
    protected void completed(@NonNull final DownloadTask task) {

    }

    @Override
    protected void canceled(@NonNull final DownloadTask task) {

    }

    @Override
    protected void error(@NonNull final DownloadTask task, @NonNull final Exception e) {

    }

    @Override
    protected void warn(@NonNull final DownloadTask task) {

    }

    @Override
    public void retry(@NonNull final DownloadTask task, @NonNull final ResumeFailedCause cause) {

    }

    @Override
    public void connected(@NonNull final DownloadTask task, final int blockCount, final long currentOffset, final long totalLength) {

    }

    @Override
    public void progress(@NonNull final DownloadTask task, final long currentOffset, final long totalLength) {

    }
}
