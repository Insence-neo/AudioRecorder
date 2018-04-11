package com.insence.audiorecorder;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.insence.audiorecorder.Listener.PlayListener;
import com.insence.audiorecorder.adapters.FileViewerAdapter;
import com.insence.audiorecorder.libs.FillSeekBar;

import java.io.IOException;

/**
 * Created by Insence on 2018/4/2.
 */

public class AsyncPlay extends AsyncTask<String, Integer, Void> {
    private static final String LOG_TAG = "AsyncPlay";

    private Boolean isPaused = false;
    private Boolean isCanceled = false;
    private FillSeekBar fillSeekBar;

    private int pause_time;
    private int lastProgress;

    public static final int TYPE_FINISH = 0;
    public static final int TYPE_PAUSED = 1;
    public static final int TYPE_CANCELED = 2;

    private PlayListener listener;
    private FileViewerAdapter.RecordingsViewHolder holder;

    private MediaPlayer mMediaPlayer = null;


    public AsyncPlay(PlayListener listener,FileViewerAdapter.RecordingsViewHolder holder) {
        this.listener = listener;
        this.holder = holder;
    }


    //该方法运行在UI线程中,可对UI控件进行设置
    @Override
    protected void onPreExecute() {
    }

    //该方法不运行在UI线程中,主要用于异步操作,通过调用publishProgress()方法
    //触发onProgressUpdate对UI进行操作
    @Override
    protected Void doInBackground(String... params) {
        try {
            mMediaPlayer = new MediaPlayer();
            //string convert to int
            final int start_time = Integer.valueOf(params[1]).intValue();
            mMediaPlayer.setDataSource(params[0]);
            mMediaPlayer.prepareAsync();
            fillSeekBar = holder.itemView.findViewById(R.id.FillSeekBar);

            //监听准备好的media
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    fillSeekBar.setMaxVal(mMediaPlayer.getDuration());
                    mMediaPlayer.seekTo(start_time);
                }
            });

            //当媒体设置seek时播放时，回掉此函数
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    while (mMediaPlayer.isPlaying()) {
                        if (isPaused) { //暂停了就返回状态
                            mMediaPlayer.pause();
                            pause_time = mMediaPlayer.getCurrentPosition();
                            mMediaPlayer.reset();
                            mMediaPlayer.release();
                            listener.onPaused(pause_time);
                        } else if (isCanceled) {
                            fillSeekBar.setProgress(mMediaPlayer.getDuration());
                            mMediaPlayer.stop();
                            mMediaPlayer.reset();
                            mMediaPlayer.release();
                            pause_time = 0;
                            listener.onCanceled(pause_time);
                        } else {
                            int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                            //调用  onProgressUpdate
                            publishProgress(mCurrentPosition);
                        }
                    }
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                pause_time = 0;
                listener.onFinish(pause_time);
            }
        });
        return null;
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
        //获得进度
        int progress = values[0];
        //若有变化
        if (progress > lastProgress) {
            //通知监听进行更新
            listener.onProgress(progress,holder);
            lastProgress = progress;
        }
    }

    public void pausePlay() {
        isPaused = true;
    }

    public void cancelPlay() {
        isCanceled = true;
    }
}
