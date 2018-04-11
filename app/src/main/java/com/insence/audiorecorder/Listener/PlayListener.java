package com.insence.audiorecorder.Listener;

import android.media.MediaPlayer;

import com.insence.audiorecorder.adapters.FileViewerAdapter;

/**
 * Created by Insence on 2018/4/2.
 */

public interface PlayListener {
    void onProgress(int progress,FileViewerAdapter.RecordingsViewHolder holder);

    void onFinish(int pause_time);

    void onPaused(int pause_time);

    void onCanceled(int pause_time);
}
