package com.insence.audiorecorder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.insence.audiorecorder.R;
import com.insence.audiorecorder.adapters.FragmentAdapter;
import com.insence.audiorecorder.fragments.FileViewerFragment;
import com.insence.audiorecorder.fragments.RecordFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    //private List<View> viewList = new ArrayList<>();
    private List<String> stringList = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    @BindView(R.id.tb)
    Toolbar tb;
    @BindView(R.id.psts)
    PagerSlidingTabStrip psts;
    @BindView(R.id.vp)
    ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Set a Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(tb);
        //initView();
        initFragments();
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentAdapter(getSupportFragmentManager());
        vp.setAdapter(fragmentPagerAdapter);
        //设置psts
        setTabsValue();
        //PagerSlidingTabStrip绑定ViewPager
        psts.setViewPager(vp);
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    //menu选择
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        psts.setShouldExpand(true);
        //pstsIndicator.setIndicatorColor(R.color.colorAccent);
    }

    private void initFragments() {
        fragmentList.add(new RecordFragment());
        stringList.add("Record");
        fragmentList.add(new FileViewerFragment());
        stringList.add("Saved");
    }
}
