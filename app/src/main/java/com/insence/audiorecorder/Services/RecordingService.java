package com.insence.audiorecorder.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Binder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.insence.audiorecorder.Helper.DBHelper;
import com.insence.audiorecorder.Interface.RecorderInterface;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.activities.MainActivity;
import com.insence.audiorecorder.fragments.FileViewerFragment;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;
import omrecorder.WriteAction;


public class RecordingService extends Service {
    private static final String LOG_TAG = "RecordingService";
    //创建OmRecoreder实例
    private Recorder recorder = null;
    private DBHelper mDatabase;
    //总时长
    private long mElapsedMillis = 0;
    //文件名 文件路径
    private String mFileName = null;
    private String mFilePath = null;
    //音频设置
    private int trackId;
    private int sampleRate;
    private int quality_imageId;
    private String track;
    //binder实例
    private RecordingBinder binder = new RecordingBinder();
    private SharedPreferences preferences;
    private boolean check_noise;
    private boolean check_autogain;

    public RecordingService() {
    }

    //广播接收接收来自 service完成播放的通知
    public class BroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mElapsedMillis = intent.getLongExtra("finish_time",0);
        }
    }

    //Service被创建时调用
    @Override
    public void onCreate() {
        super.onCreate();
        //创建数据库存储文件名 等信息
        mDatabase = new DBHelper(getApplicationContext());
        //获取音频设置
        preferences = getSharedPreferences("Data",MODE_PRIVATE);
        check_noise = preferences.getBoolean("check_noise",false);
        check_autogain = preferences.getBoolean("check_autogain",false);
        trackId = preferences.getInt("trackId",AudioFormat.CHANNEL_IN_MONO);
        sampleRate = preferences.getInt("sampleRate",16000);
        quality_imageId = preferences.getInt("quality_imageId", R.drawable.ic_quality_normal);
        track = preferences.getString("track",getString(R.string.dialog_single_track));
        //TODO创建前台通知
    }

    //Service被启动时调用
    //:onStartCommand()方法必须返回一个整数，这个整数是一个描述了在系统的杀死事件中，系统应该如何继续这个服务的值
    //START_STICKY  当Service因内存不足而被系统kill后，一段时间后内存再次空闲时，系统将会尝试重新创建此Service，
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    //onBind() 返回此 Binder 实例
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    //解除绑定时调用
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //stopService   或者服务被销毁时调用 ：写下停止录音的逻辑
    //在bindService中（服务不必自行停止运行）
    @Override
    public void onDestroy() {
        if (recorder != null) {
            stopRecording();
            //服务结束 写下数据
            try {
                mElapsedMillis = preferences.getLong("length",0);
                mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis,quality_imageId,track,sampleRate);
            } catch (Exception e){
                Log.e(LOG_TAG, "exception", e);
            }
        }
    }

    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class RecordingBinder extends Binder implements RecorderInterface {

        @Override
        public void onPause() {
            RecordingService.this.pause();
        }

        @Override
        public void onResume() {
            RecordingService.this.resume();
        }
    }


    //private
    private void startRecording() {
        setupRecorder();
        recorder.startRecording();
    }

    private void pause() {
        recorder.pauseRecording();
    }

    private void resume() {
        recorder.resumeRecording();
    }

    private void stopRecording() {
        try {
            recorder.stopRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this,  "" + mFilePath, Toast.LENGTH_LONG).show();
    }

    private void setupRecorder() {
        if(check_autogain && check_noise == false) {
            recorder = OmRecorder.wav(new PullTransport.Default(mic()),file());
        } else if (check_noise == true) {
            recorder = OmRecorder.wav(new PullTransport.Default(
                    new PullableSource.NoiseSuppressor(mic())
            ),file());
        } else if (check_autogain == true) {
            recorder = OmRecorder.wav(new PullTransport.Default(
                    new PullableSource.AutomaticGainControl(mic())
            ),file());
        } else {
            recorder = OmRecorder.wav(new PullTransport.Default(
                    new PullableSource.AutomaticGainControl(
                            new PullableSource.NoiseSuppressor(mic())
                    )
            ),file());
        }

    }

    private File file() {
        int count = 0;
        File f;
        do{
            count++;

            mFileName = "audio"
                    + "_" + (count) + ".wav";
            //获取安卓内置的内存的路径
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/AudioRecorder/" + mFileName;
            f = new File(mFilePath);
            //isDirectory()是检查一个对象是否是文件夹
        }while (f.exists() && !f.isDirectory());
        return f;
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        trackId, sampleRate
                )
        );
    }
}
