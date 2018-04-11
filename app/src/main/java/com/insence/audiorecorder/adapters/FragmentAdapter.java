package com.insence.audiorecorder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.insence.audiorecorder.fragments.FileViewerFragment;
import com.insence.audiorecorder.fragments.RecordFragment;

/**
 * Created by Insence on 2018/3/29.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    //private List<Fragment> fraglist;
    private String[] titlelist = {"Record","Saved"};

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    //展示在List中的fragment 但这里如果不同list 是调用·fragment中的newInstance 完成构造实例 根据位置
    @Override
    public Fragment getItem(int position) {
        switch ( position ) {
            case 0:
                return RecordFragment.newInstance(position);
            case 1:
                return FileViewerFragment.newInstance(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return titlelist.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titlelist[position];
    }
}
