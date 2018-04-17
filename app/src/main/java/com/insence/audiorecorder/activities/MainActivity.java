package com.insence.audiorecorder.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int PERMISSION_REQ = 0;
    private List<String> stringList = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    @BindView(R.id.tb)
    Toolbar tb;
    @BindView(R.id.psts)
    PagerSlidingTabStrip psts;
    @BindView(R.id.vp)
    ViewPager vp;
    private String[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Set a Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(tb);
        //initView();
        initFragments();
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentAdapter(getSupportFragmentManager(),getApplication());
        vp.setAdapter(fragmentPagerAdapter);
        //设置psts
        setTabsValue();
        //PagerSlidingTabStrip绑定ViewPager
        psts.setViewPager(vp);
        //获取权限
        getPermissions();
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

    private void getPermissions() {
        permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(MainActivity.this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_required),
                    PERMISSION_REQ, permissions);
        }
    }

    @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //权限处理的回掉函数
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this,"You already granted permissions",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        showRationale(requestCode,perms);
    }

    //拒绝的dialog
    private void showRationale(int requestCode,List<String> perms) {
        Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
        new AppSettingsDialog
                .Builder(this)
                .build()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(this, permissions)) {
                //这里响应的是AppSettingsDialog点击取消按钮的效果
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        psts.setShouldExpand(true);
        //pstsIndicator.setIndicatorColor(R.color.colorAccent);
    }

    private void initFragments() {
        fragmentList.add(new RecordFragment());
        stringList.add(getResources().getString(R.string.title_record));
        fragmentList.add(new FileViewerFragment());
        stringList.add(getResources().getString(R.string.title_saved));
    }
}
