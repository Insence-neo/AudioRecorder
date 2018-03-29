package com.insence.audiorecorder.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import com.insence.audiorecorder.Interface.RecorderInterface;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.Services.RecordingService;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Insence on 2018/3/29.
 */

public class RecordFragment extends Fragment {
    private long recordingTime = 0; //记录下的时间
    boolean canRecord = true;
    boolean canPause = false;
    private static final String ARG_POSITION = "position";
    private int position;

    private Intent intent;
    RecorderInterface recorderInterface;
    //conn 数据连接对象
    private RecordServiceConn conn = new RecordServiceConn();

    @BindView(R.id.chronometer)
    Chronometer mChronometer;
    @BindView(R.id.btnRecord)
    FloatingActionButton btnRecord;
    @BindView(R.id.btnStop)
    FloatingActionButton btnStop;
    Unbinder unbinder;

    public static RecordFragment newInstance(int position) {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        unbinder = ButterKnife.bind(this, view);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //可录音 不可暂停
                if (canRecord && (!canPause)) {
                    Record();
                    //状态转换
                    canRecord = !canRecord;
                    canPause = !canPause;
                }
                // 不可录音 可暂停
                else if ( (!canRecord) && canPause) {
                    PauseRecord();
                    //状态转换
                    canPause = !canPause;
                }
                else {
                    ResumeRecord();
                    //状态转换
                    canPause = !canPause;
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
     * 通过这个IBinder对象，实现宿主和Service的交互。
     */
    private class RecordServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            recorderInterface = (RecorderInterface) iBinder;//返回录音接口
        }
        //服务意外断开时回掉
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }



    //录音逻辑
    private void Record() {
        //生成intent getActivity() 是fragment中找到父activity的方法
        intent = new Intent(getActivity(), RecordingService.class);
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_pause);
        btnStop.setImageResource(R.drawable.ic_media_stop);
        Toast.makeText(getActivity(), "start record", Toast.LENGTH_SHORT).show();
        //创建文件夹
        File folder = new File(Environment.getExternalStorageDirectory() + "/AudioRecorder");
        if (!folder.exists()) {
            //boolean mkdir() 创建此抽象路径名指定的目录。
            //boolean mkdirs() 创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
            folder.mkdir();
        }

        //时钟逻辑
        mChronometer.setBase(SystemClock.elapsedRealtime() - recordingTime);
        mChronometer.start();

        // 服务逻辑
        getActivity().startService(intent);
        getActivity().bindService(intent, conn, BIND_AUTO_CREATE);

        //keep screen on while recording
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //tv.setText("recording");
    }

    private void PauseRecord() {
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_play);
        Toast.makeText(getActivity(),"Already pause", Toast.LENGTH_LONG).show();
        //时钟逻辑
        mChronometer.stop();
        // 保存这次记录了的时间
        recordingTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
        //服务逻辑
        recorderInterface.onPause();
    }

    private void ResumeRecord() {
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_pause);
        Toast.makeText(getActivity(),"Already resume", Toast.LENGTH_LONG).show();
        //时钟逻辑
        mChronometer.setBase(SystemClock.elapsedRealtime() - recordingTime);
        mChronometer.start();
        //服务逻辑
        recorderInterface.onResume();
    }

    private void stopRecord() {
        //时钟逻辑
        mChronometer.stop();
        //清零
        recordingTime = 0;
        mChronometer.setBase(SystemClock.elapsedRealtime());
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_mic_white_36dp);
        btnStop.setImageResource(R.drawable.ic_media_stop);
        //tv.setText("Tap to record");

        canRecord = true;
        canPause = false;
        //服务逻辑  解绑服务 并关闭服务
        getActivity().unbindService(conn);
        getActivity().stopService(intent);
        //allow the screen to turn off again once recording is finished
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
