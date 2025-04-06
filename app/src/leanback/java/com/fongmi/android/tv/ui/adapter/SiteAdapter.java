package com.fongmi.android.tv.ui.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.AdapterSiteBinding;

import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final List<Site> mItems;
    private int type; // 私有变量，表示当前模式 (0: 点击切换, 1: 可搜索设置, 2: 可切换设置)

    /**
     * 构造函数
     * @param listener 回调监听器
     */
    public SiteAdapter(OnClickListener listener) {
        this.mListener = listener;
        this.mItems = VodConfig.get().getSites(); // 获取所有站点配置
    }

    /**
     * 点击事件回调接口
     */
    public interface OnClickListener {
        /**
         * 当列表项被点击时调用 (仅在 type == 0 时)
         * @param item 被点击的 Site 对象
         */
        void onItemClick(Site item);
    }

    /**
     * 设置适配器的模式类型
     * @param type 模式类型 (0, 1, 2)
     */
    public void setType(int type) {
        this.type = type;
        notifyDataSetChanged(); // 通知数据变更，触发 onBindViewHolder 刷新视图
    }

    /**
     * 获取当前适配器的模式类型
     * @return 当前模式类型 (0, 1, 2)
     */
    public int getType() {
        return this.type; // 返回私有变量 type 的值
    }

    /**
     * 全选/取消全选（根据当前模式决定是启用还是禁用）
     */
    public void selectAll() {
        // 如果当前不是隐藏模式 (type 3 是假设的隐藏模式，这里应为 type == 0 ?)
        // 实际逻辑: 如果 type != 3 (或者说 type 为 1 或 2 时)，则设置为 true (全选/启用)
        // 这里逻辑似乎有点绕，原意可能是 "全选可搜索" 或 "全选可切换"
        setEnable(true); // 传递 true 给 setEnable
    }

    /**
     * 取消全选/全不选
     */
    public void cancelAll() {
         // 如果当前是隐藏模式 (type == 3)? 逻辑同上，似乎是反向操作
         // 实际逻辑: 如果 type != 3，则设置为 false (取消全选/禁用)
        setEnable(false); // 传递 false 给 setEnable
    }

    @Override
    public int getItemCount() {
        return mItems.size(); // 返回站点列表的大小
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用 ViewBinding 创建 ViewHolder
        AdapterSiteBinding binding = AdapterSiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Site item = mItems.get(position); // 获取当前位置的 Site 对象

        holder.binding.text.setText(item.getName()); // 设置站点名称
        holder.binding.check.setChecked(getChecked(item)); // 根据模式设置 CheckBox 的选中状态
        holder.binding.text.setSelected(item.isActivated()); // 设置文本是否为选中状态 (视觉效果)
        holder.binding.text.setActivated(item.isActivated()); // 设置文本是否为激活状态 (视觉效果)

        // 根据模式 (type) 决定 CheckBox 是否可见
        // type 为 0 (点击切换模式) 时隐藏 CheckBox，否则 (type 1 或 2) 显示
        holder.binding.check.setVisibility(type == 0 ? View.GONE : View.VISIBLE);

        // 设置长按监听器
        holder.binding.getRoot().setOnLongClickListener(v -> setLongListener(item));
        // 设置点击监听器
        holder.binding.getRoot().setOnClickListener(v -> setListener(item, position));

        // 根据设置的站点显示模式 (列表或网格) 设置文本对齐方式
        holder.binding.text.setGravity(Setting.getSiteMode() == 0 ? Gravity.CENTER : Gravity.START);
    }

    /**
     * 根据当前模式 (type) 判断 Site 对象对应的 CheckBox 是否应为选中状态
     * @param item Site 对象
     * @return true 如果应选中，false 如果不应选中
     */
    private boolean getChecked(Site item) {
        if (type == 1) return item.isSearchable(); // 模式 1: 返回是否可搜索
        if (type == 2) return item.isChangeable(); // 模式 2: 返回是否可切换
        return false; // 其他模式 (如 0) 返回 false
    }

    /**
     * 处理列表项的点击事件
     * @param item 被点击的 Site 对象
     * @param position 被点击项的位置
     */
    private void setListener(Site item, int position) {
        if (type == 0) {
            // 模式 0: 调用外部监听器的 onItemClick 方法
            mListener.onItemClick(item);
        } else if (type == 1) {
            // 模式 1: 切换 Searchable 状态并保存，然后刷新该项视图
            item.setSearchable(!item.isSearchable()).save();
            notifyItemChanged(position);
        } else if (type == 2) {
            // 模式 2: 切换 Changeable 状态并保存，然后刷新该项视图
            item.setChangeable(!item.isChangeable()).save();
            notifyItemChanged(position);
        }
        // 注意: type != 0 时 notifyItemChanged(position) 的原始代码在 if 外面，
        // 这里为了清晰移入了各自的 if 分支，效果相同。
        // if (type != 0) notifyItemChanged(position); // 原代码位置
    }

    /**
     * 处理列表项的长按事件 (似乎用于批量操作的触发)
     * @param item 被长按的 Site 对象
     * @return true 表示事件已处理
     */
    private boolean setLongListener(Site item) {
        // 这里的逻辑似乎是：长按某一项时，将所有项的状态设置为与该项 *相反* 的状态
        if (type == 1) setEnable(!item.isSearchable());
        if (type == 2) setEnable(!item.isChangeable());
        return true; // 返回 true 表示消费了长按事件
    }

    /**
     * 启用或禁用所有站点的某个属性 (Searchable 或 Changeable)
     * @param enable true 表示启用，false 表示禁用
     */
    private void setEnable(boolean enable) {
        List<Site> sites = VodConfig.get().getSites(); // 获取所有站点
        if (type == 1) {
            // 模式 1: 设置所有站点的 Searchable 属性
            for (Site site : sites) {
                site.setSearchable(enable).save();
            }
        } else if (type == 2) {
            // 模式 2: 设置所有站点的 Changeable 属性
            for (Site site : sites) {
                site.setChangeable(enable).save();
            }
        }
        // 通知 RecyclerView 刷新所有可见项（以及可能因复用需要更新的项）
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * ViewHolder 类，用于持有列表项的视图和绑定对象
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterSiteBinding binding; // ViewBinding 对象

        ViewHolder(@NonNull AdapterSiteBinding binding) {
            super(binding.getRoot()); // 将根视图传递给父类
            this.binding = binding;   // 保存 ViewBinding 对象
        }
    }
}
