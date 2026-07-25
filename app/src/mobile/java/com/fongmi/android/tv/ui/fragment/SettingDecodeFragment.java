package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.FragmentSettingDecodeBinding;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;

public class SettingDecodeFragment extends BaseFragment {

    private FragmentSettingDecodeBinding mBinding;

    public static SettingDecodeFragment newInstance() {
        return new SettingDecodeFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingDecodeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        refresh();
    }

    @Override
    protected void initEvent() {
        mBinding.aac.setOnClickListener(this::setAAC);
        mBinding.tunnel.setOnClickListener(this::setTunnel);
        mBinding.audioPrefer.setOnClickListener(this::setAudioPrefer);
        mBinding.videoPrefer.setOnClickListener(this::setVideoPrefer);
        mBinding.audioPassThrough.setOnClickListener(this::setAudioPassThrough);
        mBinding.dolby.setOnClickListener(this::setDolby);
        mBinding.dv7.setOnClickListener(this::setDv7);
    }

    private void refresh() {
        boolean exo = !PlayerSetting.isMpv();
        mBinding.aacText.setText(Setting.getSwitch(PlayerSetting.isPreferAAC()));
        mBinding.tunnelText.setText(Setting.getSwitch(PlayerSetting.isTunnel()));
        mBinding.audioPreferText.setText(Setting.getSwitch(PlayerSetting.isAudioPrefer()));
        mBinding.videoPreferText.setText(Setting.getSwitch(PlayerSetting.isVideoPrefer()));
        mBinding.audioPassThroughText.setText(Setting.getSwitch(PlayerSetting.isAudioPassThrough()));
        mBinding.dolbyText.setText(Setting.getSwitch(PlayerSetting.isMpvDolbyHwdecEnabled()));
        mBinding.dv7Text.setText(Setting.getSwitch(PlayerSetting.isDv7HevcFallback()));
        mBinding.aac.setVisibility(exo ? View.VISIBLE : View.GONE);
        mBinding.tunnel.setVisibility(exo ? View.VISIBLE : View.GONE);
        mBinding.audioPrefer.setVisibility(exo ? View.VISIBLE : View.GONE);
        mBinding.videoPrefer.setVisibility(exo ? View.VISIBLE : View.GONE);
        mBinding.audioPassThrough.setVisibility(exo ? View.VISIBLE : View.GONE);
        mBinding.dolby.setVisibility(exo ? View.GONE : View.VISIBLE);
        mBinding.dv7.setVisibility(exo ? View.VISIBLE : View.GONE);
    }

    private void setTunnel(View view) {
        if (PlayerSetting.isMpv()) return;
        PlayerSetting.putTunnel(!PlayerSetting.isTunnel());
        mBinding.tunnelText.setText(Setting.getSwitch(PlayerSetting.isTunnel()));
    }

    private void setAudioPassThrough(View view) {
        PlayerSetting.putAudioPassThrough(!PlayerSetting.isAudioPassThrough());
        mBinding.audioPassThroughText.setText(Setting.getSwitch(PlayerSetting.isAudioPassThrough()));
    }

    private void setAudioPrefer(View view) {
        PlayerSetting.putAudioPrefer(!PlayerSetting.isAudioPrefer());
        mBinding.audioPreferText.setText(Setting.getSwitch(PlayerSetting.isAudioPrefer()));
    }

    private void setVideoPrefer(View view) {
        PlayerSetting.putVideoPrefer(!PlayerSetting.isVideoPrefer());
        mBinding.videoPreferText.setText(Setting.getSwitch(PlayerSetting.isVideoPrefer()));
    }

    private void setAAC(View view) {
        PlayerSetting.putPreferAAC(!PlayerSetting.isPreferAAC());
        mBinding.aacText.setText(Setting.getSwitch(PlayerSetting.isPreferAAC()));
    }

    private void setDolby(View view) {
        PlayerSetting.putMpvDolbyHwdecEnabled(!PlayerSetting.isMpvDolbyHwdecEnabled());
        mBinding.dolbyText.setText(Setting.getSwitch(PlayerSetting.isMpvDolbyHwdecEnabled()));
    }

    private void setDv7(View view) {
        PlayerSetting.putDv7HevcFallback(!PlayerSetting.isDv7HevcFallback());
        mBinding.dv7Text.setText(Setting.getSwitch(PlayerSetting.isDv7HevcFallback()));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refresh();
    }
}
