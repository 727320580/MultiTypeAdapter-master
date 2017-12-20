package com.wenld.multitypeadapter.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p/>
 * Author: 温利东 on 2017/6/14 11:09.
 * http://www.jianshu.com/u/99f514ea81b3
 * github: https://github.com/LidongWen
 * <p>
 * 帮助管理布局类型
 */

public class TypePool {
    private
    @NonNull
    // 所有视图和布局的集合
    final Map<Class<?>, CopyOnWriteArrayList<MultiItemView>> calss2ItemViewMap;
    // 布局集合（类型查找）
    final Map<Integer, MultiItemView> itemViewType2itemViewMap;
    // 类型集合（布局查找）
    final Map<MultiItemView, Integer> itemViewMap2itemViewType;
    // 运营的集合
    final Map<Integer, Integer> itemViewType2RecyclerCount;

    public TypePool() {
        calss2ItemViewMap = new ConcurrentHashMap<>();
        itemViewType2itemViewMap = new ConcurrentHashMap<>();
        itemViewMap2itemViewType = new ConcurrentHashMap<>();
        itemViewType2RecyclerCount = new ConcurrentHashMap<>();
    }

    /**
     * 注册存储类型 （第一个方法，最早的方法）      11111111111111111111111
     *
     * @param clazz         数据模型
     * @param multiItemView 数据模型对应的需要的布局对象
     * @param <T>
     */
    public <T> void register(@NonNull Class<? extends T> clazz, @NonNull MultiItemView<T> multiItemView) {
        // 找到布局的集合
        CopyOnWriteArrayList<MultiItemView> list = calss2ItemViewMap.get(clazz);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
        }
        // 获取布局的集合长度 （默认将数据类型和集合放在一起使用）
        int size = itemViewType2itemViewMap.size();
        // 一个bean 对 多个 view 用到，默认走下面
        if (multiItemView.haveChild()) {
            list.addAll(multiItemView.getChildList());

            for (MultiItemView<T> tMultiItemView : multiItemView.getChildList()) {
                itemViewType2itemViewMap.put(size, tMultiItemView);
                itemViewMap2itemViewType.put(tMultiItemView, size);
                size++;
            }
        } else {
            list.add(multiItemView);
            itemViewType2itemViewMap.put(size, multiItemView);
            itemViewMap2itemViewType.put(multiItemView, size);
        }
        calss2ItemViewMap.put(clazz, list);
    }

    /**
     * 通过Adapter的数据模型找出对应的布局的Id
     *
     * @param item     adapter 的数据模型
     * @param position
     * @param <T>
     * @return
     */
    public <T> int getItemViewType(@NonNull T item, int position) {
        Class<?> clazz = item.getClass();
        CopyOnWriteArrayList<MultiItemView> list = calss2ItemViewMap.get(clazz);
        for (MultiItemView multiItemView : list) {
            // 默认返回数据
            if (multiItemView.isForViewType(item, position)) {
                return itemViewMap2itemViewType.get(multiItemView);
            }
        }
        return -1;
    }

    /**
     * 获取数据模型对应的布局类型 （在adapter开始绘制的时候，调用次方法来回去我们需要调用的布局）
     *
     * @param itemViewType 数据的类型（通过adapter的getAdapter找到对应的数据类型）
     * @return 布局
     */
    public MultiItemView getMultiItemView(int itemViewType) {
        return itemViewType2itemViewMap.get(itemViewType);
    }

    /**
     * 设置缓存级别，保证数据流畅不会内存异常
     *
     * @param recyclerView
     * @param itemType
     */
    public void setMaxRecycledViews(ViewGroup recyclerView, int itemType) {
        // 如果数据缓存集合不包含 这种数据 ， 直接找到视图，并且设置缓存级别；
        if (!itemViewType2RecyclerCount.containsKey(itemType)) {
            MultiItemView multiItemView = itemViewType2itemViewMap.get(itemType);
            itemViewType2RecyclerCount.put(itemType, multiItemView.getMaxRecycleCount());
            ((RecyclerView) recyclerView).getRecycledViewPool().setMaxRecycledViews(itemType, multiItemView.getMaxRecycleCount());
        }
    }
}
