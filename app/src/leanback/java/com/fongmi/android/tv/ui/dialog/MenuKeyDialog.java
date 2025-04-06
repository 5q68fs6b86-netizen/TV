package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
// 移除不再需要的 import
// import android.view.LayoutInflater;
// import android.view.WindowManager;
// import androidx.appcompat.app.AlertDialog; // MaterialAlertDialogBuilder 会处理
// import androidx.recyclerview.widget.GridLayoutManager;
// import com.fongmi.android.tv.Setting;
// import com.fongmi.android.tv.databinding.DialogMenuBinding;
// import com.fongmi.android.tv.ui.adapter.MenuAdapter;
// import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.impl.MenuKeyCallback;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// 移除 MenuAdapter 相关依赖
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

// 不再需要实现 MenuAdapter.OnClickListener
public class MenuKeyDialog {
    // 移除不再需要的成员变量
    // private final DialogMenuBinding binding;
    // private final MenuAdapter adapter;
    // private final AlertDialog dialog;

    // 保留 callback 和 activity 引用
    private final MenuKeyCallback callback;
    private final Activity activity;

    // 保留工厂方法 create
    public static MenuKeyDialog create(Activity activity) {
        return new MenuKeyDialog(activity);
    }

    // 保留构造函数，用于获取 activity 和 callback
    public MenuKeyDialog(Activity activity) {
        // 确保调用者实现了回调接口
        if (!(activity instanceof MenuKeyCallback)) {
            // 抛出异常，明确指出问题
            throw new ClassCastException(activity.toString() + " 必须实现 MenuKeyCallback 接口");
        }
        this.activity = activity;
        this.callback = (MenuKeyCallback) activity;
        // 移除加载布局、创建 Adapter、创建旧 Dialog 的代码
        // this.binding = DialogMenuBinding.inflate(LayoutInflater.from(activity));
        // this.adapter = new MenuAdapter(this, mItems); // mItems 获取逻辑移到 show()
        // this.dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).create();
    }

    // show() 方法的实现改为构建 MaterialAlertDialogBuilder
    public void show() {
        // 1. 数据准备: 获取完整的字符串数组
        final String[] items = ResUtil.getStringArray(R.array.select_home_menu_key);

        // 如果没有菜单项，则不执行任何操作
        if (items == null || items.length == 0) {
            return;
        }

        // 2. 使用 MaterialAlertDialogBuilder 构建和显示对话框
        new MaterialAlertDialogBuilder(this.activity) // 使用存储的 activity 上下文
                .setTitle(R.string.setting_vod_menu) // 设置标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 3. 处理点击事件: 使用存储的 callback
                        // 'which' 直接对应 'items' 数组中的索引
                        MenuKeyDialog.this.callback.onMenuKeyItemClick(which);
                        // 对话框点击后会自动关闭
                    }
                })
                // 添加取消按钮
                .setNegativeButton(android.R.string.cancel, null)
                .show(); // 显示对话框

        // 原来的 initView() 方法及其调用的 setRecyclerView() 和 setDialog() 不再需要
    }

    // 移除不再需要的方法
    // private int getCount() { ... }
    // private float getWidth() { ... }
    // private void initView() { ... }
    // private void setRecyclerView() { ... }
    // private void setDialog() { ... }
    // @Override public void onItemClick(int position) { ... } // 点击逻辑移到上面匿名类中
}