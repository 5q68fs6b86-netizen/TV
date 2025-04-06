package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log; // 引入 Log 用于调试输出
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull; // 确保导入，可能 ToggleGroup 监听器需要
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R; // 假设按钮 ID 在 R 文件中定义
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.DialogSiteBinding;
import com.fongmi.android.tv.impl.SiteCallback;
import com.fongmi.android.tv.ui.adapter.SiteAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.fongmi.android.tv.utils.ResUtil; // 假设 ResUtil 包含 dp2px 和 getScreenWidth
// 移除了 com.fongmi.android.tv.utils.Util (如果不再需要 hideSystemUI 等)
import com.google.android.material.button.MaterialButtonToggleGroup; // 引入 ToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SiteDialog implements SiteAdapter.OnClickListener {

    private static final String TAG = SiteDialog.class.getSimpleName(); // Log Tag

    private RecyclerView.ItemDecoration decoration;
    private final DialogSiteBinding binding;
    private final SiteCallback callback;
    private final SiteAdapter adapter;
    private final AlertDialog dialog;
    // private int type; // 移除 type 字段，状态由 ToggleGroup 管理
    private int initialMode = 0; // 用于设置初始模式 (0: Normal, 1: Search, 2: Change)

    public static SiteDialog create(Activity activity) {
        return new SiteDialog(activity);
    }

    public SiteDialog(Activity activity) {
        this.adapter = new SiteAdapter(this); // 传入 this 作为监听器
        // 确保调用此对话框的 Activity 实现了 SiteCallback 接口，否则会抛出异常
        try {
            this.callback = (SiteCallback) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, activity.getClass().getSimpleName() + " must implement SiteCallback", e);
            // 在实际应用中可能需要更优雅地处理这个错误，例如抛出自定义异常或返回 null
            throw new ClassCastException(activity.toString() + " must implement SiteCallback");
        }
        this.binding = DialogSiteBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot()) // 使用自定义布局
                .create();
    }

    /**
     * 设置对话框初始模式为搜索模式。应在 show() 之前调用。
     */
    public SiteDialog search() {
        this.initialMode = 1; // 1 代表搜索模式
        return this;
    }

    /**
     * （可选）如果布局中有额外的操作区域，使其可见。应在 show() 之前调用。
     */
    public SiteDialog action() {
        // 确保 binding.action 在布局中存在且 ID 正确
        if (binding.action != null) {
            binding.action.setVisibility(View.VISIBLE);
        } else {
             Log.w(TAG, "binding.action is null, cannot set visibility.");
        }
        return this;
    }

    /**
     * 配置并显示对话框。
     */
    public void show() {
        // 1. 设置 ToggleGroup 的初始选中状态
        // 假设 R.id.buttonSearchMode 和 R.id.buttonChangeMode 是 ToggleGroup 中按钮的 ID
        try {
            if (initialMode == 1) {
                binding.modeToggleGroup.check(R.id.buttonSearchMode);
                adapter.setType(1); // 同步 Adapter 的初始类型
            } else if (initialMode == 2) { // 当前没有方法设置 initialMode = 2，但保留逻辑
                binding.modeToggleGroup.check(R.id.buttonChangeMode);
                adapter.setType(2); // 同步 Adapter 的初始类型
            } else {
                binding.modeToggleGroup.clearChecked(); // 默认模式，不选中任何按钮
                adapter.setType(0); // 同步 Adapter 的初始类型
            }
        } catch (NullPointerException e) {
             Log.e(TAG, "Error setting initial toggle group state. Check XML IDs (modeToggleGroup, buttonSearchMode, buttonChangeMode).", e);
             // 可能需要设置默认类型以防出错
             adapter.setType(0);
        }


        // 2. 设置 Select/Cancel 按钮的初始可用状态
        updateActionButtonsState(adapter.getType());

        // 3. 初始化视图组件 (RecyclerView, 列表/网格模式按钮)
        initView();

        // 4. 初始化事件监听器 (包括 ToggleGroup 的监听器)
        initEvent();

        // 5. 设置对话框尺寸并显示 (在视图和事件设置完毕后)
        setDialog();
    }

    // --- 辅助方法：计算布局参数 ---

    private boolean list() {
        // 如果项目少于10个或者用户设置为列表模式，则使用列表视图
        return Setting.getSiteMode() == 0 || adapter.getItemCount() < 10;
    }

    private int getCount() {
        int itemCount = adapter.getItemCount();
        if (itemCount == 0) return 1; // 没有项目时默认为1列
        // 如果是列表模式返回1列，否则根据项目数量计算1到3列
        return list() ? 1 : Math.max(1, Math.min((int) Math.ceil(itemCount / 10.0f), 3));
    }

    private int getIcon() {
        // 根据当前是列表模式还是网格模式返回不同的图标资源 ID
        return list() ? R.drawable.ic_site_grid : R.drawable.ic_site_list;
    }

    private float getWidth() {
        // 基础宽度 40%，每增加一列（最多到3列）增加 20%
        return 0.4f + (getCount() - 1) * 0.2f;
    }

    // --- 初始化方法 ---

    private void initView() {
        setRecyclerView(); // 配置 RecyclerView
        setMode();         // 配置列表/网格模式切换按钮
        // setDialog() 移至 show() 方法末尾调用
    }

    private void initEvent() {
        // 列表/网格模式切换按钮点击事件
        binding.mode.setOnClickListener(this::setMode);

        // 全选/取消全选按钮点击事件 (确保按钮在布局中存在)
        if (binding.select != null) {
            binding.select.setOnClickListener(v -> adapter.selectAll());
        }
         if (binding.cancel != null) {
            binding.cancel.setOnClickListener(v -> adapter.cancelAll());
        }

        // 模式切换 ToggleGroup (Search/Change) 状态变化监听
        // 确保 R.id.buttonSearchMode 和 R.id.buttonChangeMode 是正确的按钮 ID
         try {
             binding.modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                int currentMode = 0; // 默认普通模式
                 if (isChecked) { // 只在有按钮被选中时处理
                     if (checkedId == R.id.buttonSearchMode) {
                         currentMode = 1; // 搜索模式
                     } else if (checkedId == R.id.buttonChangeMode) {
                         currentMode = 2; // 更改模式
                     }
                 }
                 // 当用户取消选中（例如再次点击已选中的按钮，如果 singleSelection=false）
                 // 或者当 checkedId 不是我们期望的按钮时，currentMode 保持为 0
                 adapter.setType(currentMode); // 更新 Adapter 中的模式
                 updateActionButtonsState(currentMode); // 更新 Select/Cancel 按钮状态
             });
         } catch (NullPointerException e) {
              Log.e(TAG, "Error adding listener to modeToggleGroup. Check XML ID.", e);
         }
    }

    // --- UI 更新方法 ---

    private void setRecyclerView() {
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setItemAnimator(null);

        // 移除旧的 ItemDecoration (如果存在)
        if (decoration != null) {
            binding.recycler.removeItemDecoration(decoration);
        }

        // 添加新的 ItemDecoration (间距)
        int columns = getCount(); // 获取当前应有的列数
        // 使用 ResUtil.dp2px 获取像素值，保证间距单位正确
        decoration = new SpaceItemDecoration(columns, ResUtil.dp2px(16));
        binding.recycler.addItemDecoration(decoration);

        // 设置 LayoutManager
        binding.recycler.setLayoutManager(new GridLayoutManager(dialog.getContext(), columns));

        // 滚动到上次选中的位置 (如果列表不为空且按钮没有焦点)
        if (!binding.mode.hasFocus() && adapter.getItemCount() > 0) {
            int homeIndex = VodConfig.getHomeIndex();
            // 检查索引有效性
            if (homeIndex >= 0 && homeIndex < adapter.getItemCount()) {
                // 使用 post 确保在布局计算完成后滚动
                binding.recycler.post(() -> binding.recycler.scrollToPosition(homeIndex));
            }
        }
    }

    private void setDialog() {
        // 显示对话框并设置宽度，恢复 Material 默认背景遮罩
        try {
            if (dialog.getWindow() != null) {
                 WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                 params.width = (int) (ResUtil.getScreenWidth() * getWidth());
                 // 推荐使用 setLayout 设置尺寸，比 setAttributes 更稳定
                 dialog.getWindow().setLayout(params.width, WindowManager.LayoutParams.WRAP_CONTENT);

                 // 移除 dialog.getWindow().setDimAmount(0); 以使用默认遮罩效果
            } else {
                 Log.e(TAG, "Dialog window is null, cannot set layout params.");
            }
            // 显示对话框
            dialog.show();
        } catch (Exception e) {
             // 捕获可能的异常，例如 WindowManager$BadTokenException
             Log.e(TAG, "Error setting dialog layout params or showing dialog", e);
             // 可以考虑在此处显示一个 Toast 提示用户出错
             // Toast.makeText(binding.getRoot().getContext(), "无法显示站点对话框", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 配置列表/网格模式切换按钮的状态和图标
     */
    private void setMode() {
        int itemCount = adapter.getItemCount();
        boolean enableToggle = itemCount >= 20; // 只有项目多于等于20个时才允许切换模式

        // 如果不允许切换，强制设为列表模式 (Setting.putSiteMode(0))
        if (!enableToggle) {
            Setting.putSiteMode(0);
            // 如果强制为列表模式，可能需要重新计算 getCount() 并更新 RecyclerView 布局？
            // 目前的逻辑是 setMode(View) 点击时才更新布局，这里可以考虑是否需要立即更新。
        }

        binding.mode.setEnabled(enableToggle); // 设置按钮是否可点击
        binding.mode.setImageResource(getIcon()); // 根据当前模式设置按钮图标
    }

    /**
     * 处理列表/网格模式切换按钮的点击事件
     */
    private void setMode(View view) {
        // 切换存储的模式设置 (0 变 1, 1 变 0)
        Setting.putSiteMode(Setting.getSiteMode() == 0 ? 1 : 0);
        // 重新初始化视图以应用新的模式（主要是 RecyclerView 的列数变化）
        initView(); // 这会调用 setRecyclerView，使用新的 getCount()
        // 确保按钮状态（图标和是否可用）在切换后也更新
        setMode();
    }

    /**
     * 更新 Select All / Cancel All 按钮的可用状态 (Enabled/Disabled)
     * @param currentMode 当前的模式 (0: Normal, 1: Search, 2: Change)
     */
    private void updateActionButtonsState(int currentMode) {
        boolean actionsEnabled = currentMode > 0; // 只有在 Search 或 Change 模式下才启用
        // 确保按钮在布局中存在
         if (binding.select != null) {
            binding.select.setEnabled(actionsEnabled); // 使用 setEnabled
        }
         if (binding.cancel != null) {
            binding.cancel.setEnabled(actionsEnabled); // 使用 setEnabled
        }
    }

    // --- SiteAdapter.OnClickListener 接口实现 ---

    @Override
    public void onItemClick(Site item) {
        // 通过回调将选中的 Site 对象传递给 Activity
        callback.setSite(item);
        // 关闭对话框
        dialog.dismiss();
    }
}
