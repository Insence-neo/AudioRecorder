package com.insence.audiorecorder.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.insence.audiorecorder.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Insence on 2018/4/10.
 */

public class SettingsFragment extends Fragment {


    @BindView(R.id.cd_noise)
    CardView cdNoise;
    @BindView(R.id.cd_info)
    CardView cdInfo;
    @BindView(R.id.toggle_noise)
    Switch toggle_Noise;
    @BindView(R.id.toggle_autogain)
    Switch toggle_Autogain;
    @BindView(R.id.cd_automaticGain)
    CardView cdAutomaticGain;


    private View rootView;
    Unbinder unbinder;

    Boolean isCheck;
    SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        preferences = getActivity().getSharedPreferences("Data", MODE_PRIVATE);
        editor = getActivity().getSharedPreferences("Data", MODE_PRIVATE).edit();
        //初始化
        toggle_Noise.setChecked(noiseIsChecked());
        toggle_Autogain.setChecked(autoGainIsChecked());

        cdNoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = !noiseIsChecked();
                toggle_Noise.setChecked(checked);
                editor.putBoolean("check_noise", checked);
                editor.apply();
            }
        });

        toggle_Noise.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean checked = !noiseIsChecked();
                toggle_Noise.setChecked(checked);
                editor.putBoolean("check_noise", checked);
                editor.apply();
            }
        });

        cdAutomaticGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = !autoGainIsChecked();
                toggle_Autogain.setChecked(checked);
                editor.putBoolean("check_autogain", checked);
                editor.apply();
            }
        });

        toggle_Autogain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean checked = !autoGainIsChecked();
                toggle_Autogain.setChecked(checked);
                editor.putBoolean("check_autogain", checked);
                editor.apply();
            }
        });


        cdInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LicensesFragment licensesFragment = new LicensesFragment();
                licensesFragment.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog_licenses");
            }
        });

        return rootView;
    }

    private boolean autoGainIsChecked() {
        return preferences.getBoolean("check_autogain", false);
    }

    private boolean noiseIsChecked() {
        return preferences.getBoolean("check_noise", false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
