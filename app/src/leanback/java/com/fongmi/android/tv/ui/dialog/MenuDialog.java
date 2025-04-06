package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface; // 导入正确的 OnClickListener
import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.activity.HistoryActivity;
import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.fongmi.android.tv.ui.activity.KeepActivity;
import com.fongmi.android.tv.ui.activity.LiveActivity;
import com.fongmi.android.tv.ui.activity.PushActivity;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.activity.SettingActivity;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 类本身不再需要实现接口，可以简化为一个工具类或保留原来的结构但移除无用成员
public class MenuDialog {

    /**
     * 显示 Material Design 风格的简单列表菜单对话框。
     *
     * @param activity 上下文 Activity
     */
    public static void showUsingSetItems(Activity activity) {
        // 1. 数据准备 (与原代码逻辑一致)
        String[] allItems = ResUtil.getStringArray(R.array.select_home_menu_key);
        List<String> itemsToShowList = new ArrayList<>(Arrays.asList(allItems));

        // 移除第一个元素 (关键步骤，保持与原点击逻辑的索引一致)
        if (!itemsToShowList.isEmpty()) {
            itemsToShowList.remove(0);
        }

        // 转换为数组，因为 setItems 需要数组
        String[] itemsToShow = itemsToShowList.toArray(new String[0]);

        // 如果没有菜单项，则不显示对话框
        if (itemsToShow.length == 0) {
            return;
        }

        // 2. 使用 MaterialAlertDialogBuilder 构建和显示对话框
        new MaterialAlertDialogBuilder(activity)
                // .setTitle(R.string.menu_title) // 可选: 设置对话框标题
                .setItems(itemsToShow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 3. 处理点击事件
                        // 'which' 参数就是被点击项在 itemsToShow 数组中的索引
                        handleItemClick(activity, which);
                        // dialog 会在点击后自动 dismiss，通常无需手动调用 dialog.dismiss()
                    }
                })
                // .setNegativeButton(android.R.string.cancel, null) // 可选: 添加取消按钮
                .show(); // 显示对话框
    }

    /**
     * 处理菜单项点击事件的私有辅助方法。
     *
     * @param activity 上下文 Activity
     * @param position 被点击项的索引 (对应 itemsToShow 数组)
     */
    private static void handleItemClick(Activity activity, int position) {
        // 这里的 position 逻辑与原代码中的 position 完全一致，
        // 因为我们处理的是已经移除了第一个元素后的列表/数组。
        if (position == 0 && activity instanceof HomeActivity) {
            SiteDialog.create(activity).show();
        } else if (position == 1 && activity instanceof HomeActivity) {
            HistoryDialog.create(activity).type(0).show();
        } else if (position == 2) {
            LiveActivity.start(activity);
        } else if (position == 3) {
            HistoryActivity.start(activity);
        } else if (position == 4) {
            SearchActivity.start(activity);
        } else if (position == 5) {
            PushActivity.start(activity);
        } else if (position == 6) {
            KeepActivity.start(activity);
        } else if (position == 7) {
            SettingActivity.start(activity);
        }
        // 注意: 确保这里的 position 最大值与 itemsToShow 数组的大小匹配
    }
}