package com.fongmi.android.tv.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.viewbinding.ViewBinding;

// 导入 Glide 相关类
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
import com.fongmi.android.tv.databinding.ActivitySettingBinding; // 确保这里的 Binding 类名与你的布局文件名对应 (activity_setting.xml -> ActivitySettingBinding)
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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity implements BackupCallback, ConfigCallback, SiteCallback, LiveCallback, DohCallback, ProxyCallback {

    private ActivitySettingBinding mBinding; // 确保类型正确
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
        // 使用你的 Binding 类来初始化
        return mBinding = ActivitySettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.vod.requestFocus(); // 注意: 如果根布局变为 ConstraintLayout，初始焦点可能需要重新考虑或设置到 ScrollView 里的第一个元素
        mBinding.vodUrl.setText(VodConfig.getDesc());
        mBinding.liveUrl.setText(LiveConfig.getDesc());
        mBinding.wallUrl.setText(WallConfig.getDesc());
        mBinding.dohText.setText(getDohList()[getDohIndex()]);
        mBinding.versionText.setText(BuildConfig.VERSION_NAME);
        mBinding.proxyText.setText(UrlUtil.scheme(Setting.getProxy()));
        mBinding.backupText.setText((backup = ResUtil.getStringArray(R.array.select_backup))[Setting.getBackupMode()]);
        mBinding.aboutText.setText(BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_api + "-" + BuildConfig.FLAVOR_abi);
        setCacheText();

        // --- 新增：加载右下角的 WebP 动画 ---
        loadAnimatedWebp();
        // ----------------------------------
    }

    // --- 新增：加载 WebP 动画的方法 ---
    private void loadAnimatedWebp() {
        // 检查 binding 和 ImageView 是否为空，避免空指针
        if (mBinding != null && mBinding.animatedWebpView != null) {
            Glide.with(this) // 使用 Activity 作为上下文
                    .asGif() // 将动画 WebP 作为 GIF 处理以确保循环
                    .load(R.drawable.dynamic_animation) // 加载你的 WebP 文件资源
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // 缓存策略
                    .into(mBinding.animatedWebpView); // 设置到对应的 ImageView
        }
    }
    // ----------------------------------

    private void setCacheText() {
        FileUtil.getCacheSize(new Callback() {
            @Override
            public void success(String result) {
                // 添加空检查，因为回调可能在 Activity 销毁后执行
                if (mBinding != null) {
                    mBinding.cacheText.setText(result);
                }
            }
        });
    }

    @Override
    protected void initEvent() {
        // 确保这里的 ID 仍然能通过 mBinding 正确访问到
        mBinding.vod.setOnClickListener(this::onVod);
        mBinding.live.setOnClickListener(this::onLive);
        mBinding.wall.setOnClickListener(this::onWall);
        mBinding.proxy.setOnClickListener(this::onProxy);
        mBinding.cache.setOnClickListener(this::onCache);
        mBinding.cache.setOnLongClickListener(this::onCacheLongClick);
        mBinding.backup.setOnClickListener(this::onBackup);
        mBinding.restore.setOnClickListener(this::onRestore);
        mBinding.player.setOnClickListener(this::onPlayer);
        mBinding.danmu.setOnClickListener(this::onDanmu);
        mBinding.version.setOnClickListener(this::onVersion);
        mBinding.vod.setOnLongClickListener(this::onVodEdit);
        mBinding.vodHome.setOnClickListener(this::onVodHome);
        mBinding.live.setOnLongClickListener(this::onLiveEdit);
        mBinding.liveHome.setOnClickListener(this::onLiveHome);
        mBinding.wall.setOnLongClickListener(this::onWallEdit);
        mBinding.backup.setOnLongClickListener(this::onBackupMode);
        mBinding.vodHistory.setOnClickListener(this::onVodHistory);
        mBinding.version.setOnLongClickListener(this::onVersionDev);
        mBinding.liveHistory.setOnClickListener(this::onLiveHistory);
        mBinding.wallDefault.setOnClickListener(this::setWallDefault);
        mBinding.wallRefresh.setOnClickListener(this::setWallRefresh);
        mBinding.custom.setOnClickListener(this::onCustom);
        mBinding.doh.setOnClickListener(this::setDoh);
        mBinding.about.setOnClickListener(this::onAbout);
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
        // 添加空检查
        if (mBinding == null) return;
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
                // 确保 Activity 仍然存活
                if (!isFinishing()) {
                   setConfig();
                }
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
                 // 确保 Activity 仍然存活
                if (!isFinishing()) {
                   setConfig();
                }
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
        // 原来的空实现
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
                // 确保 Activity 仍然存活且 Binding 存在
                 if (!isFinishing() && mBinding != null) {
                    setCacheText();
                 }
            }
             @Override
            public void error(String msg) { // 也处理错误情况
                 Notify.dismiss();
                 Notify.show(msg);
            }
        });
    }

    private void onCustom(View view) {
        SettingCustomActivity.start(this);
    }

    private void onAbout(View view) {
        // 添加空检查
        if (mBinding != null) {
            mBinding.aboutText.setText(BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_api + "-" + BuildConfig.FLAVOR_abi);
        }
    }

    private void setDoh(View view) {
        DohDialog.create(this).index(getDohIndex()).show();
    }

    @Override
    public void setDoh(Doh doh) {
        // 添加空检查
        if (mBinding == null) return;
        Source.get().stop();
        OkHttp.get().setDoh(doh);
        Notify.progress(getActivity()); // getActivity() 在 Activity 中就是 this
        Setting.putDoh(doh.toString());
        mBinding.dohText.setText(doh.getName());
        VodConfig.load(Config.vod(), getCallback());
    }

    private void onProxy(View view) {
        ProxyDialog.create(this).show();
    }

    @Override
    public void setProxy(String proxy) {
        // 添加空检查
        if (mBinding == null) return;
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
                 // 确保 Activity 仍然存活且 Binding 存在
                if (!isFinishing() && mBinding != null) {
                    VodConfig.get().getConfig().json("").save(); // 清除缓存也清除 VOD 配置的 JSON 缓存
                    setCacheText();
                }
            }
        });
    }

    private boolean onCacheLongClick(View view) {
        FileUtil.clearCache(new Callback() {
            @Override
            public void success() {
                 // 确保 Activity 仍然存活且 Binding 存在
                if (!isFinishing() && mBinding != null) {
                    setCacheText();
                    Config config = VodConfig.get().getConfig().json("").save(); // 清除缓存并清空 JSON
                    if (!config.isEmpty()) { // 如果保存后的配置不为空（理论上应该是空的）
                         // 这里可能不需要再调用 setConfig(config)，因为目的是清空
                         // 如果需要重新加载默认或之前的配置，逻辑需要调整
                         // 简单起见，长按清空后可能不需要自动加载
                    } else {
                         // 可选：如果清空后需要刷新界面或加载默认配置，在此处处理
                         // RefreshEvent.config(); // 例如，可以发个事件让主界面刷新
                    }
                }
            }
        });
        return true;
    }

    @Override
    public void restore(File file) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) { // 只有获取到权限才执行恢复
                AppDatabase.restore(file, new Callback() {
                    @Override
                    public void success() {
                         Notify.progress(getActivity());
                         App.post(() -> {
                            if (!isFinishing()){ // 延迟后再次检查 Activity 是否还在
                                AppDatabase.reset(); // 重置数据库连接（如果需要）
                                initConfig(); // 重新加载配置
                            }
                         }, 3000); // 延迟执行以确保文件操作完成
                    }
                    @Override
                    public void error(String msg) {
                         Notify.show(getString(R.string.error_restore) + ": " + msg);
                    }
                });
            } else {
                 Notify.show(R.string.error_permission); // 提示权限不足
            }
        });
    }

    private void onRestore(View view) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) BackupDialog.create(this).show();
             else Notify.show(R.string.error_permission);
        });
    }

    private void initConfig() {
        // 初始化并加载配置
        WallConfig.get().init(); // 初始化壁纸
        LiveConfig.get().init().load(); // 初始化并加载直播
        VodConfig.get().init().load(getCallback()); // 初始化并加载点播，使用回调处理结果
    }

    private void onBackup(View view) {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request((allGranted, grantedList, deniedList) -> {
             if (allGranted){
                 AppDatabase.backup(new Callback() {
                    @Override
                    public void success(String path) {
                        Notify.show(getString(R.string.backed_to) + " " + path); // 显示备份路径
                    }
                     @Override
                    public void error(String msg) {
                        Notify.show(getString(R.string.error_backup) + ": " + msg);
                    }
                });
             } else {
                  Notify.show(R.string.error_permission);
             }
        });
    }

    private boolean onBackupMode(View view) {
        // 添加空检查
        if (mBinding == null || backup == null || backup.length == 0) return true; // 防止空指针
        int index = Setting.getBackupMode();
        index = index >= backup.length - 1 ? 0 : ++index; // 修正边界条件
        Setting.putBackupMode(index);
        mBinding.backupText.setText(backup[index]);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        super.onRefreshEvent(event); // 调用父类的方法（如果父类有实现）
        // 添加空检查
        if (mBinding == null || event == null) return;

        switch (event.getType()) {
            case CONFIG:
                setCacheText();
                mBinding.vodUrl.setText(VodConfig.getDesc());
                mBinding.liveUrl.setText(LiveConfig.getDesc());
                mBinding.wallUrl.setText(WallConfig.getDesc());
                // 可能还需要刷新 DoH 和 Proxy 显示
                mBinding.dohText.setText(getDohList()[getDohIndex()]);
                mBinding.proxyText.setText(UrlUtil.scheme(Setting.getProxy()));
                break;
            case VIDEO:
                 // 如果需要在视频源变化时更新设置界面某项，在这里处理
                 break;
             case HISTORY:
                  // 如果需要在历史记录变化时更新设置界面某项，在这里处理
                  break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 可以在这里停止 Glide 加载，虽然 Glide 通常会自动处理
        if (mBinding != null && mBinding.animatedWebpView != null) {
             Glide.with(this).clear(mBinding.animatedWebpView);
        }
        mBinding = null; // 释放 Binding 对象，防止内存泄漏
        // RefreshEvent.history(); // 这个事件的发送位置可能需要根据具体逻辑调整，看是否真的需要在设置页销毁时刷新历史记录
    }
}