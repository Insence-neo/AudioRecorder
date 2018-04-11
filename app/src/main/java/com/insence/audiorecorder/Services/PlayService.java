package com.insence.audiorecorder.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.insence.audiorecorder.AsyncPlay;
import com.insence.audiorecorder.Listener.PlayListener;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.RecordingItem;
import com.insence.audiorecorder.adapters.FileViewerAdapter;
import com.insence.audiorecorder.libs.FillSeekBar;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlayService extends Service {
    private static final String LOG_TAG = "PlaybackFragment";
    private TextView text_progress;
    private FillSeekBar fillSeekBar;
    private ImageView image_state;
    private MediaPlayer mMediaPlayer;
    private Intent intent = new Intent("com.insence.play_state.RECEIVER");
    private Handler mHandler = new Handler();

    public PlayService() {
    }

    private PlayBinder mBinder = new PlayBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //暴露给activity binder使用的方法
    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
        public void play(FileViewerAdapter.RecordingsViewHolder holder, RecordingItem item) {
            startPlay(holder,item);
        }
        public void pause() {
            pausePlay();
        }
        public void resume() {
            resumePlay();
        }
        public void stop() { stopPlay();}
    }

    public void startPlay(FileViewerAdapter.RecordingsViewHolder holder, RecordingItem item) {
        fillSeekBar = holder.itemView.findViewById(R.id.FillSeekBar);
        text_progress = holder.itemView.findViewById(R.id.play_progress_text);
        image_state = holder.itemView.findViewById(R.id.image_state);
        text_progress.setVisibility(View.VISIBLE);
        image_state.setImageResource(R.drawable.ic_play_pause);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(item.getFilePath());
            mMediaPlayer.prepare();
            fillSeekBar.setMaxVal(mMediaPlayer.getDuration());
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        //监听完成
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlay();
            }
        });
        updateSeekBar();
        //发出广播 提醒activity 开始播放
        intent.putExtra("isPlaying",true);
        intent.putExtra("isPause",false);
        sendBroadcast(intent);
    }

    public void pausePlay() {
        image_state.setImageResource(R.drawable.ic_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
        //发出广播 提醒activity 暂停
        intent.putExtra("isPlaying",false);
        intent.putExtra("isPause",true);
        sendBroadcast(intent);
    }

    public void resumePlay() {
        image_state.setImageResource(R.drawable.ic_play_pause);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
        //发出广播 提醒activity 暂停
        intent.putExtra("isPlaying",true);
        intent.putExtra("isPause",false);
        sendBroadcast(intent);
    }

    private void stopPlay() {
        image_state.setImageResource(R.drawable.ic_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        fillSeekBar.setProgress(0);
        //消失进度时间
        text_progress.setVisibility(View.GONE);
        //发出广播 提醒activity完成播放
        intent.putExtra("isPlaying",false);
        intent.putExtra("isPause",false);
        sendBroadcast(intent);
    }

    //updating mSeekBar
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mMediaPlayer != null){
                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                fillSeekBar.setProgress(mCurrentPosition);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                text_progress.setText(String.format("%02d:%02d", minutes, seconds));
                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1);
    }
}
