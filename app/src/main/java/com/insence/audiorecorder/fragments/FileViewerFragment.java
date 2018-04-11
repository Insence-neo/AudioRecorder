package com.insence.audiorecorder.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.insence.audiorecorder.R;
import com.insence.audiorecorder.RecordingItem;
import com.insence.audiorecorder.Services.PlayService;
import com.insence.audiorecorder.adapters.FileViewerAdapter;
import com.insence.audiorecorder.adapters.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Insence on 2018/3/29.
 */

public class FileViewerFragment extends Fragment {
    private FileViewerAdapter mFileViewerAdapter;
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";
    private FileViewerAdapter fileViewerAdapter;
    private int position;

    private PlayService.PlayBinder playBinder;
    private PlayService playService;
    private FileViewerAdapter.RecordingsViewHolder holder;
    private FileViewerAdapter.RecordingsViewHolder  lastholder;
    RecordingItem item;
    private boolean isPlaying = false;
    private boolean isPause =false;
    private Intent intent;
    BroadCast receiver;
    public static Fragment newInstance(int position) {
        FileViewerFragment fragment = new FileViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION,position);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
     * 通过这个IBinder对象，实现宿主和Service的交互。
     */
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playBinder = (PlayService.PlayBinder) iBinder;
            playService=playBinder.getService();
        }

        //服务意外结束会回掉
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isPlaying = false;
            isPause = false;
        }
    };

    //广播接收接收来自 service完成播放的通知
    public class BroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isPause = intent.getBooleanExtra("isPause",false);
            isPlaying = intent.getBooleanExtra("isPlaying",false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
        //注册广播
        receiver = new BroadCast();//实例化
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.insence.play_state.RECEIVER");
        getActivity().registerReceiver(receiver, intentFilter);
        //绑定服务
        intent = new Intent(getActivity(), PlayService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        //填充recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rlv);
        //初始化布局管理器 默认是纵向
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //newest to oldest order (database stores from oldest to newest)
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置默认动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //设置适配器
        fileViewerAdapter = new FileViewerAdapter(getActivity(),layoutManager);
        recyclerView.setAdapter(fileViewerAdapter);

        fileViewerAdapter.setOnItemClickListener(new FileViewerAdapter.OnItemClickListener() {
            //click 从Adapter传来holder and item
            @Override
            public void onClick(int position, FileViewerAdapter.RecordingsViewHolder holder,RecordingItem item) {
                //设置从adapter传来的(holder,item);
                setHolderAndItem(holder,item);
                FileViewerFragment.this.onClick();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(connection);
        getActivity().stopService(intent);
        //注销广播
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    //private
    private void onClick() {
        //首先是否是上一次点击的item
        if (this.holder != lastholder) {  //点击不同
            //没结束（播着或者暂停) 先停掉在播放 初始化状态
            if (isPlaying || isPause) {
                playBinder.stop();
                playBinder.play(holder,item);
            } else {  //没放 直接播放
                playBinder.play(holder,item);
            }
            lastholder = holder;
        } else if (this.holder == lastholder) {  //点击了同一个
            if (isPlaying) {
                //执行暂停
                playBinder.pause();
            } else if (isPause){
                // 执行继续
                playBinder.resume();
            } else {
                //播放
                playBinder.play(holder,item);
            }
        }
    }

    private void setHolderAndItem(FileViewerAdapter.RecordingsViewHolder holder, RecordingItem item) {
        this.holder = holder;
        this.item = item;
    }

    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if(event == FileObserver.DELETE){
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}
