package com.fongmi.android.tv.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.Updater;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.ActivitySettingBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.BackupCallback;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.impl.DohCallback;
import com.fongmi.android.tv.impl.LiveCallback;
import com.fongmi.android.tv.impl.ProxyCallback;
import com.fongmi.android.tv.impl.SiteCallback;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.dialog.BackupDialog;
import com.fongmi.android.tv.ui.dialog.ConfigDialog;
import com.fongmi.android.tv.ui.dialog.DohDialog;
import com.fongmi.android.tv.ui.dialog.HistoryDialog;
import com.fongmi.android.tv.ui.dialog.LiveDialog;
import com.fongmi.android.tv.ui.dialog.ProxyDialog;
import com.fongmi.android.tv.ui.dialog.SiteDialog;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.bean.Doh;
import com.github.catvod.net.OkHttp;
import com.permissionx.guolindev.PermissionX;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity implements BackupCallback, ConfigCallback, SiteCallback, LiveCallback, DohCallback, ProxyCallback {

    private ActivitySettingBinding mBinding;
    private String[] backup;
    private int type;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingActivity.class));
    }

    private int getDohIndex() {
        return Math.max(0, VodConfig.get().getDoh().indexOf(Doh.objectFrom(Setting.getDoh())));
    }

    private String[] getDohList() {
        List<String> list = new ArrayList<>();
        for (Doh item : VodConfig.get().getDoh()) list.add(item.getName());
        return list.toArray(new String[0]);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recycler.setAdapter(new SettingAdapter());
    }

    private void setCacheText() {
        FileUtil.getCacheSize(new Callback() {
            @Override
            public void success(String result) {
                mBinding.cacheText.setText(result);
            }
        });
    }

    @Override
    protected void initEvent() {
    }

    @Override
    public void setConfig(Config config) {
        if (config.getUrl().startsWith("file") && !PermissionX.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> load(config));
        } else {
            load(config);
        }
    }

    private void load(Config config) {
        switch (config.getType()) {
            case 0:
                Notify.progress(this);
                VodConfig.load(config, getCallback());
                mBinding.vodUrl.setText(config.getDesc());
                break;
            case 1:
                Notify.progress(this);
                LiveConfig.load(config, getCallback());
                mBinding.liveUrl.setText(config.getDesc());
                break;
            case 2:
                Notify.progress(this);
                WallConfig.load(config, getCallback());
                mBinding.wallUrl.setText(config.getDesc());
                break;
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success(String result) {
                Notify.show(result);
            }

            @Override
            public void success() {
                setConfig();
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
                setConfig();
            }
        };
    }

    private void setConfig() {
        switch (type) {
            case 0:
                Notify.dismiss();
                RefreshEvent.history();
                RefreshEvent.config();
                RefreshEvent.video();
                break;
            case 1:
                Notify.dismiss();
                RefreshEvent.config();
                break;
            case 2:
                Notify.dismiss();
                RefreshEvent.config();
                break;
        }
    }

    @Override
    public void setSite(Site item) {
        VodConfig.get().setHome(item);
        RefreshEvent.video();
    }

    @Override
    public void onChanged() {
    }

    @Override
    public void setLive(Live item) {
        LiveConfig.get().setHome(item);
    }

    private void onVod(View view) {
        ConfigDialog.create(this).type(type = 0).show();
    }

    private void onLive(View view) {
        ConfigDialog.create(this).type(type = 1).show();
    }

    private void onWall(View view) {
        ConfigDialog.create(this).type(type = 2).show();
    }

    private boolean onVodEdit(View view) {
        ConfigDialog.create(this).type(type = 0).edit().show();
        return true;
    }

    private boolean onLiveEdit(View view) {
        ConfigDialog.create(this).type(type = 1).edit().show();
        return true;
    }

    private boolean onWallEdit(View view) {
        ConfigDialog.create(this).type(type = 2).edit().show();
        return true;
    }

    private void onVodHome(View view) {
        SiteDialog.create(this).action().show();
    }

    private void onLiveHome(View view) {
        LiveDialog.create(this).action().show();
    }

    private void onVodHistory(View view) {
        HistoryDialog.create(this).type(type = 0).show();
    }

    private void onLiveHistory(View view) {
        HistoryDialog.create(this).type(type = 1).show();
    }

    private void onPlayer(View view) {
        SettingPlayerActivity.start(this);
    }

    private void onDanmu(View view) {
        SettingDanmuActivity.start(this);
    }

    private void onVersion(View view) {
        Updater.get().force().release().start(this);
    }

    private boolean onVersionDev(View view) {
        Updater.get().force().dev().start(this);
        return true;
    }

    private void setWallDefault(View view) {
        WallConfig.refresh(Setting.getWall() == 4 ? 1 : Setting.getWall() + 1);
    }

    private void setWallRefresh(View view) {
        Notify.progress(this);
        WallConfig.get().load(new Callback() {
            @Override
            public void success() {
                Notify.dismiss();
                setCacheText();
            }
        });
    }

    private void onCustom(View view) {
        SettingCustomActivity.start(this);
    }

    private void onAbout(View view) {
        mBinding.aboutText.setText(BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_api + "-" + BuildConfig.FLAVOR_abi);
    }

    private void setDoh(View view) {
        DohDialog.create(this).index(getDohIndex()).show();
    }

    @Override
    public void setDoh(Doh doh) {
        Source.get().stop();
        OkHttp.get().setDoh(doh);
        Notify.progress(getActivity());
        Setting.putDoh(doh.toString());
        mBinding.dohText.setText(doh.getName());
        VodConfig.load(Config.vod(), getCallback());
    }

    private void onProxy(View view) {
        ProxyDialog.create(this).show();
    }

    @Override
    public void setProxy(String proxy) {
        Source.get().stop();
        Setting.putProxy(proxy);
        OkHttp.selector().clear();
        OkHttp.get().setProxy(proxy);
        Notify.progress(getActivity());
        VodConfig.load(Config.vod(), getCallback());
        mBinding.proxyText.setText(UrlUtil.scheme(proxy));
    }

    private void onCache(View view) {
        FileUtil.clearCache(new Callback() {
            @Override
            public void success() {
                VodConfig.get().getConfig().json("").save();
                setCacheText();
            }
        });
    }

    private boolean onCacheLongClick(View view) {
        FileUtil.clearCache(new Callback() {
            @Override
            public void success() {
                setCacheText();
                Config config = VodConfig.get().getConfig().json("").save();
                if (!config.isEmpty()) setConfig(config);
            }
        });
        return true;
    }

    @Override
    public void restore(File file) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> AppDatabase.restore(file, new Callback() {
            @Override
            public void success() {
                if (allGranted) {
                    Notify.progress(getActivity());
                    App.post(() -> {
                        AppDatabase.reset();
                        initConfig();
                    }, 3000);
                }
            }
        }));
    }

    private void onRestore(View view) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) BackupDialog.create(this).show();
        });
    }

    private void initConfig() {
        WallConfig.get().init();
        LiveConfig.get().init().load();
        VodConfig.get().init().load(getCallback());
    }

    private void onBackup(View view) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> AppDatabase.backup(new Callback() {
            @Override
            public void success(String path) {
                Notify.show(R.string.backed);
            }
        }));
    }

    private boolean onBackupMode(View view) {
        int index = Setting.getBackupMode();
        Setting.putBackupMode(index = index == backup.length - 1 ? 0 : ++index);
        mBinding.backupText.setText(backup[index]);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        super.onRefreshEvent(event);
        switch (event.getType()) {
            case CONFIG:
                setCacheText();
                mBinding.vodUrl.setText(VodConfig.getDesc());
                mBinding.liveUrl.setText(LiveConfig.getDesc());
                mBinding.wallUrl.setText(WallConfig.getDesc());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefreshEvent.history();
        try {
            Glide.with(this).onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

        private final List<String> mItems;

        public SettingAdapter() {
            mItems = new ArrayList<>();
            mItems.add(ResUtil.getString(R.string.setting_vod));
            mItems.add(ResUtil.getString(R.string.setting_live));
            mItems.add(ResUtil.getString(R.string.setting_wall));
            mItems.add(ResUtil.getString(R.string.setting_player));
            mItems.add(ResUtil.getString(R.string.setting_danmu));
            mItems.add(ResUtil.getString(R.string.setting_custom));
            mItems.add(ResUtil.getString(R.string.setting_proxy));
            mItems.add(ResUtil.getString(R.string.setting_backup));
            mItems.add(ResUtil.getString(R.string.setting_restore));
            mItems.add(ResUtil.getString(R.string.setting_cache));
            mItems.add(ResUtil.getString(R.string.setting_doh));
            mItems.add(ResUtil.getString(R.string.setting_version));
            mItems.add(ResUtil.getString(R.string.setting_about));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_setting_md3, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String text = mItems.get(position);
            holder.title.setText(text);
            holder.value.setVisibility(View.VISIBLE);
            holder.switchWidget.setVisibility(View.GONE);
            if (text.equals(ResUtil.getString(R.string.setting_vod))) {
                holder.value.setText(VodConfig.getDesc());
                holder.itemView.setOnClickListener(v -> onVod(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_live))) {
                holder.value.setText(LiveConfig.getDesc());
                holder.itemView.setOnClickListener(v -> onLive(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_wall))) {
                holder.value.setText(WallConfig.getDesc());
                holder.itemView.setOnClickListener(v -> onWall(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_player))) {
                holder.value.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> onPlayer(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_danmu))) {
                holder.value.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> onDanmu(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_custom))) {
                holder.value.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> onCustom(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_proxy))) {
                holder.value.setText(UrlUtil.scheme(Setting.getProxy()));
                holder.itemView.setOnClickListener(v -> onProxy(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_backup))) {
                holder.value.setText(backup[Setting.getBackupMode()]);
                holder.itemView.setOnClickListener(v -> onBackup(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_restore))) {
                holder.value.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> onRestore(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_cache))) {
                setCacheText(holder.value);
                holder.itemView.setOnClickListener(v -> onCache(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_doh))) {
                holder.value.setText(getDohList()[getDohIndex()]);
                holder.itemView.setOnClickListener(v -> setDoh(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_version))) {
                holder.value.setText(BuildConfig.VERSION_NAME);
                holder.itemView.setOnClickListener(v -> onVersion(v));
            } else if (text.equals(ResUtil.getString(R.string.setting_about))) {
                holder.value.setText(BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_api + "-" + BuildConfig.FLAVOR_abi);
                holder.itemView.setOnClickListener(v -> onAbout(v));
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView title;
            private final TextView value;
            private final SwitchMaterial switchWidget;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                value = itemView.findViewById(R.id.value);
                switchWidget = itemView.findViewById(R.id.switch_widget);
            }
        }
    }
}
