package com.wenld.multitypeadapter.wrapper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.wenld.multitypeadapter.MultiTypeAdapter;
import com.wenld.multitypeadapter.base.MultiItemView;
import com.wenld.multitypeadapter.base.OnItemClickListener;
import com.wenld.multitypeadapter.base.ViewHolder;
import com.wenld.multitypeadapter.bean.GroupStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenld on 2017/10/3.
 * <p>
 * 备注:本身自带
 */

public class GroupWrapper extends RecyclerView.Adapter<ViewHolder> implements OnItemClickListener {
    // 多类型适配器
    MultiTypeAdapter multiTypeAdapter = new MultiTypeAdapter();
    // 父的数据
    List<GroupStructure> groupList;
    // 打开的集合的内容
    List<GroupStructure> openedList = new ArrayList<>();
    // 分组的展开和收起
    private IExpandListener listener;
    // 需要拿来展示的数据
    private List<Object> expandList;
    // 转进来需要显示数据的recycleView
    RecyclerView recyclerView;

    // 数据类型的注册
    public <T> GroupWrapper register(@NonNull Class<? extends T> clazz, @NonNull MultiItemView<T> multiItemView) {
        multiTypeAdapter.register(clazz, multiItemView);
        return this;
    }

    // 分组的适配器的初始化
    public GroupWrapper(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        multiTypeAdapter.setOnItemClickListener(this);
    }

    // 根据数据类型分配不同的布局
    @Override
    public int getItemViewType(int position) {
        return judgeType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return multiTypeAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        multiTypeAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return multiTypeAdapter.getItemCount();
    }

    // 判断类型
    public int judgeType(int position) {
        return multiTypeAdapter.getItemViewType(position);
    }

    // 填充数据
    public List<GroupStructure> getGroupList() {
        return groupList;
    }

    // 填充数据
    public void setGroupList(List<GroupStructure> groupList) {
        this.groupList = groupList;
        calculateList();
        multiTypeAdapter.setItems(expandList);
    }

    // 填充数据
    private void calculateList() {
        // 判断基础数据是否初始化
        if (expandList == null) {
            expandList = new ArrayList<>();
        }
        expandList.clear();
        // 对象的适配
        GroupStructure objGroupStructure;
        boolean isEqual = false;
        lableBreak:
        // 循环设置父的内容
        for (int j = 0; j < groupList.size(); j++) {
            // 判断是否打开过
            isEqual = false;
            // 获取到每一个布局的数据
            objGroupStructure = groupList.get(j);
            for (int i = 0; i < openedList.size(); i++) {
                if (objGroupStructure.equalParent(openedList.get(i).parent)) {
                    isEqual = true;
                    break;
                }
            }

            if (objGroupStructure.hasHeader()) {
                expandList.add(objGroupStructure.parent);
            }

            if (isEqual) {
                if (objGroupStructure.getChildrenCount() > 0) {
                    expandList.addAll(objGroupStructure.children);
                }
            }
        }
    }

    public void setListener(IExpandListener listener) {
        this.listener = listener;
    }

    public List<GroupStructure> getOpenedList() {
        return openedList;
    }

    /**
     * 获取展示的数据
     *
     * @return
     */
    public List<Object> getExpandList() {
        return expandList;
    }

    /**
     * 判断是否是分组头部
     *
     * @param position
     * @return
     */
    public boolean isGroupHeader(int position) {
        Object currentObj = expandList.get(position);
        for (GroupStructure group : groupList) {
            if (group.equalParent(currentObj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 展开数据
     *
     * @param groupPosition
     */
    public void expandOrShrikGroup(int groupPosition) {
        GroupStructure groupStructure = groupList.get(groupPosition);
        int position = -1;
        for (int i = 0; i < expandList.size(); i++) {
            Object group = expandList.get(i);
            if (group == groupStructure.parent) {
                position = i;
                break;
            }
        }

        expandOrShrikGroup(recyclerView.findViewHolderForPosition(position), groupStructure.parent, position);
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, Object o, int position) {
        expandOrShrikGroup(holder, o, position);
    }

    /**
     * 展开（收拢数据）数据
     *
     * @param holder
     * @param o
     * @param position
     */
    private void expandOrShrikGroup(RecyclerView.ViewHolder holder, Object o, int position) {
        boolean needopen = true;
        for (GroupStructure groupStructure : openedList) {
            if (groupStructure.equalParent(o)) {
                needopen = false;
                openedList.remove(groupStructure);
                if (listener != null) {
                    listener.onShrink(holder, o, position);
                }
                break;
            }
        }
        if (needopen) {
            for (GroupStructure needAddGroupStrure : groupList) {
                if (needAddGroupStrure.equalParent(o)) {
                    openedList.add(needAddGroupStrure);
                    if (listener != null) {
                        listener.onExpand(holder, o, position);
                    }
                    if (needAddGroupStrure.getChildrenCount() > 0) {
                        calculateList();
                        notifyDataSetChanged();
                    }
                    break;
                }
            }
        } else {
            calculateList();
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, Object o, int position) {
        return false;
    }

    public interface IExpandListener {
        void onExpand(RecyclerView.ViewHolder holder, Object o, int position);

        void onShrink(RecyclerView.ViewHolder holder, Object o, int position);
    }
}
