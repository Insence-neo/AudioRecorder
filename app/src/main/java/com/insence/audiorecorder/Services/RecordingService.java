package com.insence.audiorecorder.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Binder;
import android.widget.Toast;

import com.insence.audiorecorder.Interface.RecorderInterface;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class RecordingService extends Service {
    private static final String LOG_TAG = "RecordingService";
    //创建OmRecoreder实例
    private Recorder recorder = null;
    //    //创建实例
//    private MediaRecorder mRecorder = null;
    //记录录音时间
    private long mStartingTimeMillis = 0;
    //文件名 文件路径
    private String mFileName = null;
    private String mFilePath = null;
    //binder实例
    private RecordingBinder binder = new RecordingBinder();

    public RecordingService() {
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

    //onBind() 返回此 Binder 实例
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Service被创建时调用
    @Override
    public void onCreate() {
        super.onCreate();
        //创建数据库存储文件名 等信息
    }

    //Service被启动时调用
    //:onStartCommand()方法必须返回一个整数，这个整数是一个描述了在系统的杀死事件中，系统应该如何继续这个服务的值
    //START_STICKY  当Service因内存不足而被系统kill后，一段时间后内存再次空闲时，系统将会尝试重新创建此Service，
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
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
        }
        super.onDestroy();
    }


    //private
    private void startRecording() {
        setupRecorder();
        recorder.startRecording();
        //记录录音时长
        mStartingTimeMillis = System.currentTimeMillis();
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
            recorder = OmRecorder.wav(new PullTransport.Default(mic()),flie());
        //wav方法有两个参数 PullTransport 和File
        //PullTransport有多个构造函数（音频原mic(),监听音频的接口（实现音频监听的逻辑））
    }

    private File flie() {
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
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }
}
