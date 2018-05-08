package com.insence.audiorecorder.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.insence.audiorecorder.Interface.RecorderInterface;
import com.insence.audiorecorder.OptionItem;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.Services.RecordingService;
import com.insence.audiorecorder.adapters.OptionAdapter;
import com.scalified.fab.ActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Insence on 2018/3/29.
 */

public class RecordFragment extends Fragment {

    private long recordingTime = 0; //记录下的时间
    private long statTime = 0;
    boolean canRecord = true;
    boolean canPause = false;
    private static final String ARG_POSITION = "position";
    private int position;
    private long length;
    private Intent intent;
    private Context mContext;
    RecorderInterface recorderInterface;
    //conn 数据连接对象
    private RecordServiceConn conn = new RecordServiceConn();
    //SharedPreferences
    SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.cd_track)
    CardView cdTrack;
    @BindView(R.id.cd_qulity)
    CardView cdQulity;
    @BindView(R.id.btnRecord)
    ActionButton btnRecord;
    @BindView(R.id.chronometer)
    Chronometer mChronometer;
    @BindView(R.id.btnStop)
    ActionButton btnStop;
    @BindView(R.id.image_cardview_track)
    ImageView imageCardviewTrack;
    @BindView(R.id.image_cardview_quality)
    ImageView imageCardviewQuality;
    @BindView(R.id.tv_cardview_track)
    TextView tvCardviewTrack;
    @BindView(R.id.tv_cardview_quality)
    TextView tvCardviewQuality;
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

    /**
     * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
     * 通过这个IBinder对象，实现宿主和Service的交互。
     */
    private class RecordServiceConn implements ServiceConnection {

        //Activity与Service连接成功时回调该方法
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            recorderInterface = (RecorderInterface) iBinder;//返回录音接口
        }

        //Activity与Service断开连接时回掉
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        unbinder = ButterKnife.bind(this, view);
        //初始化按钮
        initializeBtnSetting();
        preferences = mContext.getSharedPreferences("Data",MODE_PRIVATE);
        imageCardviewTrack.setImageResource(preferences.getInt("track_imageId",R.drawable.ic_track));
        tvCardviewTrack.setText(preferences.getString("track",mContext.getString(R.string.dialog_single_track)));
        imageCardviewQuality.setImageResource(preferences.getInt("quality_imageId",R.drawable.ic_quality_normal));
        tvCardviewQuality.setText(preferences.getString("quality",mContext.getString(R.string.dialog_normal)));

        editor = mContext.getSharedPreferences("Data", MODE_PRIVATE).edit();

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
                // 不可录音 可暂停(播放状态)
                else if ((!canRecord) && canPause) {
                    PauseRecord();
                    //状态转换
                    canPause = !canPause;
                } else {
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

        cdTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_track));
                List<OptionItem> items = new ArrayList<>();
                items.add(new OptionItem(R.drawable.ic_track, mContext.getString(R.string.dialog_single_track), mContext.getString(R.string.dialog_single_track_caption)));
                items.add(new OptionItem(R.drawable.ic_dual_track, mContext.getString(R.string.dialog_dual_track), mContext.getString(R.string.dialog_dual_track_caption)));
                OptionAdapter adapter = new OptionAdapter(items, mContext);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //存储SharedPreference   param1：文件名
                                editor.putString("track", mContext.getString(R.string.dialog_single_track));
                                editor.putInt("track_imageId",R.drawable.ic_track);
                                editor.putInt("trackId", AudioFormat.CHANNEL_IN_MONO);
                                editor.apply();
                                //视图
                                imageCardviewTrack.setImageResource(R.drawable.ic_track);
                                tvCardviewTrack.setText(R.string.dialog_single_track);
                                break;
                            case 1:
                                editor.putString("track", mContext.getString(R.string.dialog_dual_track));
                                editor.putInt("track_imageId",R.drawable.ic_dual_track);
                                editor.putInt("trackId", AudioFormat.CHANNEL_IN_STEREO);
                                editor.apply();
                                imageCardviewTrack.setImageResource(R.drawable.ic_dual_track);
                                tvCardviewTrack.setText(R.string.dialog_dual_track);
                                break;
                        }
                    }
                });
                //设置对话框可取消
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        cdQulity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_quality));
                List<OptionItem> items = new ArrayList<>();
                items.add(new OptionItem(R.drawable.ic_quality_normal, mContext.getString(R.string.dialog_normal), mContext.getString(R.string.dialog_normal_caption)));
                items.add(new OptionItem(R.drawable.ic_quality_high, mContext.getString(R.string.dialog_high), mContext.getString(R.string.dialog_high_caption)));
                items.add(new OptionItem(R.drawable.ic_quality_premuim, mContext.getString(R.string.dialog_premium), mContext.getString(R.string.dialog_premium_caption)));
                OptionAdapter adapter = new OptionAdapter(items, mContext);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //存储SharedPreference   param1：文件名
                                editor.putString("quality", mContext.getString(R.string.dialog_normal));
                                editor.putInt("quality_imageId",R.drawable.ic_quality_normal);
                                editor.putInt("sampleRate", 16000);
                                editor.apply();
                                //视图
                                imageCardviewQuality.setImageResource(R.drawable.ic_quality_normal);
                                tvCardviewQuality.setText(R.string.dialog_normal);
                                break;
                            case 1:
                                editor.putString("quality", mContext.getString(R.string.dialog_high));
                                editor.putInt("quality_imageId",R.drawable.ic_quality_high);
                                editor.putInt("sampleRate", 22050);
                                editor.apply();
                                //视图
                                imageCardviewQuality.setImageResource(R.drawable.ic_quality_high);
                                tvCardviewQuality.setText(R.string.dialog_high);
                                break;
                            case 2:
                                editor.putString("quality", mContext.getString(R.string.dialog_premium));
                                editor.putInt("quality_imageId",R.drawable.ic_quality_premuim);
                                editor.putInt("sampleRate", 44100);
                                editor.apply();
                                //视图
                                imageCardviewQuality.setImageResource(R.drawable.ic_quality_premuim);
                                tvCardviewQuality.setText(R.string.dialog_premium);
                        }
                    }
                });
                //设置对话框可取消
                builder.setCancelable(true);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view;
    }

    private void initializeBtnSetting() {
        btnRecord.setSize(150.0f);
        btnRecord.setImageResource(R.drawable.ic_mic);
        btnRecord.setImageSize(50);
        btnStop.setSize(75f);
        btnStop.setImageResource(R.drawable.ic_media_stop);
        btnStop.setImageSize(30);
        // To set button color for normal state:
        btnRecord.setButtonColor(getResources().getColor(R.color.colorPrimary));
        btnStop.setButtonColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //录音逻辑
    private void Record() {
        //生成intent getActivity() 是fragment中找到父activity的方法
        intent = new Intent(mContext, RecordingService.class);
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_pause);
        btnStop.setImageResource(R.drawable.ic_media_stop);
        btnStop.setEnabled(true);
        mChronometer.setTextColor(getResources().getColor(R.color.Black));
        tv.setText(R.string.Recording);
        tv.setVisibility(View.VISIBLE);
        //.makeText(getActivity(), "start record", Toast.LENGTH_SHORT).show();
        //创建文件夹
        File folder = new File(Environment.getExternalStorageDirectory() + "/AudioRecorder");
        if (!folder.exists()) {
            //boolean mkdir() 创建此抽象路径名指定的目录。
            //boolean mkdirs() 创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
            folder.mkdir();
        }
        //时钟逻辑
        statTime = SystemClock.elapsedRealtime();
        mChronometer.setBase(SystemClock.elapsedRealtime() - recordingTime);
        int hour = (int) ((SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000 / 60);
        mChronometer.setFormat("0" + String.valueOf(hour) + ":%s");
        mChronometer.start();
        // 服务逻辑
        mContext.startService(intent);
        mContext.bindService(intent, conn, BIND_AUTO_CREATE);

        //keep screen on while recording
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void PauseRecord() {
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_play);
        tv.setText(R.string.Paused);
        //Toast.makeText(getActivity(), "Already pause", Toast.LENGTH_LONG).show();
        //时钟逻辑
        mChronometer.stop();
        // 保存这次记录了的时间
        recordingTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
        //服务逻辑
        recorderInterface.onPause();
    }

    private void ResumeRecord() {
        statTime = SystemClock.elapsedRealtime();
        //视图逻辑
        btnRecord.setImageResource(R.drawable.ic_media_pause);
        tv.setText(R.string.Recording);
        //Toast.makeText(getActivity(), "Already resume", Toast.LENGTH_LONG).show();
        //时钟逻辑
        mChronometer.setBase(SystemClock.elapsedRealtime() - recordingTime);
        mChronometer.start();
        //服务逻辑
        recorderInterface.onResume();
    }

    private void stopRecord() {
        if (!canRecord) {
            if ((!canRecord) && canPause) {
                //写下记录总时长
                length = recordingTime +(SystemClock.elapsedRealtime() - statTime);
            } else {
                length = recordingTime;
            }
            //写入记录
            editor.putLong("length", length);
            editor.apply();
            //视图逻辑
            mChronometer.stop();
            //清零
            recordingTime = 0;
            mChronometer.setTextColor(getResources().getColor(R.color.ChronometerColor));
            mChronometer.setBase(SystemClock.elapsedRealtime());
            btnRecord.setImageResource(R.drawable.ic_mic);
            btnStop.setImageResource(R.drawable.ic_media_stop);
            tv.setVisibility(View.INVISIBLE);
            canRecord = true;
            canPause = false;
            //服务逻辑  解绑服务 并关闭服务
            mContext.unbindService(conn);
            mContext.stopService(intent);
            //时钟逻辑
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Toast.makeText(mContext, "Please record firstly", Toast.LENGTH_SHORT).show();
        }
    }
}
